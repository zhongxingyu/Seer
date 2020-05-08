 package nl.ttva66.server;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.ejb.Stateless;
 import javax.jws.WebService;
 import javax.jws.soap.SOAPBinding;
 import javax.jws.soap.SOAPBinding.Style;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import nl.ttva66.dto.DagDto;
 import nl.ttva66.dto.TypeDto;
 import nl.ttva66.dto.ZaaldienstDto;
 import nl.ttva66.entities.Dag;
 import nl.ttva66.entities.Dienst;
 import nl.ttva66.entities.Open;
 import nl.ttva66.entities.Type;
 import nl.ttva66.entities.Zaaldienst;
 import nl.ttva66.interfaces.DagRequest;
 import nl.ttva66.interfaces.ZaalDienstRequest;
 import nl.ttva66.libary.API;
 
 @Stateless
@WebService(serviceName = "Service", targetNamespace = "http://www.ttva66.nl/wsdl", endpointInterface = "nl.ttva66.server.Service")
 @SOAPBinding(style = Style.DOCUMENT)
 public class ServiceBean implements Service {
 
 	@PersistenceContext(name = "zaaldienst")
 	private EntityManager em;
 
 	@Override
 	public TypeDto createType(TypeDto type) {
 		try {
 			Type tp = new Type();
 			tp.setEind(type.getEind());
 			tp.setNaam(type.getNaam());
 			tp.setStart(type.getStart());
 
 			em.persist(tp);
 
 			Query result = em.createQuery("select X from Dag");
 
 			@SuppressWarnings("unchecked")
 			List<Dag> dag = result.getResultList();
 
 			for (Dag deel : dag) {
 				Open open = new Open();
 				open.setOpen(false);
 				open.setType(tp);
 				open.setDag(deel);
 
 				em.persist(open);
 			}
 
 			return Convert.TypeToDto(tp);
 		} catch (Exception e) {
 			API.createIssue("Service SQL exception in createType",
 					"A service SQL exception has encountered", e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public DagDto saveDag(DagDto dag) {
 		try {
 			Dag dg = Convert.DtoToDag(dag, em);
 
 			for (Open op : dg.getOpens()) {
 				if (op.getId() != null)
 					em.merge(op);
 				else
 					em.persist(op);
 			}
 
 			ArrayList<Integer> nw = new ArrayList<Integer>();
 
 			for (Dienst dienst : dg.getDiensts()) {
 				System.out.println("Dienst: "
 						+ dienst.getZaaldienst().getNaam());
 
 				if (dienst.getId() != null)
 					em.merge(dienst);
 				else
 					em.persist(dienst);
 
 				nw.add(dienst.getId());
 			}
 			Query rs;
 			if (nw.size() > 0) {
 				rs = em.createQuery("SELECT x FROM Dienst as x WHERE dag = :dag AND id NOT IN(:dienst)");
 				rs.setParameter("dienst", nw);
 			} else {
 				rs = em.createQuery("SELECT x FROM Dienst as x WHERE dag = :dag");
 			}
 			rs.setParameter("dag", dg);
 
 			@SuppressWarnings("unchecked")
 			List<Dienst> tp = rs.getResultList();
 
 			for (Dienst del : tp) {
 				System.out.println("Deleting " + del.getZaaldienst().getNaam());
 				em.remove(del);
 			}
 
 			em.merge(dg);
 			return Convert.DagToDto(dg);
 		} catch (Exception e) {
 			API.createIssue("Service SQL exception in saveDag",
 					"A service SQL exception has encountered", e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public DagDto getDagByDate(DagRequest datum) {
 		try {
 			System.out.println("Getting data for datum: " + datum);
 			Query result = em
 					.createQuery("select X from Dag as X where dag = :dag and maand = :maand and jaar = :jaar");
 			result.setParameter("dag", datum.getDag());
 			result.setParameter("maand", datum.getMaand());
 			result.setParameter("jaar", datum.getJaar());
 
 			try {
 				Dag zt = (Dag) result.getSingleResult();
 				return Convert.DagToDto(zt);
 			} catch (NoResultException e) {
 
 			}
 			System.out.println("Date not exists yet");
 
 			// No date found yet, lets create a standard date (And persist it!)
 			Dag dag = new Dag();
 
 			Query rs = em.createQuery("select X from Type as X");
 
 			dag.setDag(datum.getDag());
 			dag.setMaand(datum.getMaand());
 			dag.setJaar(datum.getJaar());
 
 			em.persist(dag);
 
 			@SuppressWarnings("unchecked")
 			List<Type> tp = rs.getResultList();
 
 			Set<Open> op = new HashSet<Open>();
 			for (Type deel : tp) {
 				Open open = new Open();
 				open.setOpen(false);
 				open.setType(deel);
 				open.setDag(dag);
 
 				op.add(open);
 
 				em.persist(open);
 			}
 			dag.setOpens(op);
 			em.persist(dag);
 
 			System.out.println("size: " + dag.getOpens().size());
 
 			return Convert.DagToDto(dag);
 		} catch (Exception e) {
 			API.createIssue("Service SQL exception in getDagByDate",
 					"A service SQL exception has encountered", e);
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	public ZaaldienstDto getZaaldienstById(ZaalDienstRequest request) {
 		try {
 			System.out.println("Getting request for: " + request.getId());
 			Query result = em
 					.createQuery("select X from Zaaldienst as X where id = :id");
 			result.setParameter("id", request.getId());
 
 			try {
 				Zaaldienst zt = (Zaaldienst) result.getSingleResult();
 				System.out.println("Zaaldienst is hier: " + zt);
 				System.out.println("ID: " + zt.getId());
 				return Convert.ZaaldienstToDto(zt);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			System.out.println("Missing item???");
 			return null;
 		} catch (Exception e) {
 			API.createIssue("Service SQL exception in getZaaldienstById",
 					"A service SQL exception has encountered", e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public ZaaldienstDto login(String user, String password) {
 		try {
 			System.out.println(String.format("User: %s Passwd: $s", user,
 					password));
 
 			Query result = em
 					.createQuery("select X from Zaaldienst as X where email = :user and password = :password and canLogin = true");
 			result.setParameter("user", user);
 			result.setParameter("password", password);
 
 			try {
 				Zaaldienst zt = (Zaaldienst) result.getSingleResult();
 				return Convert.ZaaldienstToDto(zt);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		} catch (Exception e) {
 			API.createIssue("Service SQL exception in login",
 					"A service SQL exception has encountered", e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public Integer[] listZaaldiensten() {
 		try {
 			Query result = em.createQuery("select X from Zaaldienst as X");
 
 			@SuppressWarnings("unchecked")
 			List<Zaaldienst> zd = result.getResultList();
 			Integer[] list = new Integer[zd.size()];
 
 			int i = 0;
 			for (Zaaldienst z : zd) {
 				list[i] = z.getId();
 				i++;
 			}
 
 			return list;
 		} catch (Exception e) {
 			API.createIssue("Service SQL exception in listZaaldiensten",
 					"A service SQL exception has encountered", e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public ZaaldienstDto saveZaaldienst(ZaaldienstDto dienst) {
 		try {
 			Zaaldienst zd = Convert.DtoToZaaldienst(dienst);
 
 			if (zd.getId() != null)
 				em.merge(zd);
 			else
 				em.persist(zd);
 
 			return Convert.ZaaldienstToDto(zd);
 		} catch (Exception e) {
 			API.createIssue("Service SQL exception in saveZaaldienst",
 					"A service SQL exception has encountered", e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public TypeDto[] listTypes() {
 		try {
 			Query result = em.createQuery("select X from Type as X");
 
 			@SuppressWarnings("unchecked")
 			List<Type> zd = result.getResultList();
 			TypeDto[] list = new TypeDto[zd.size()];
 
 			int i = 0;
 			for (Type z : zd) {
 				list[i] = Convert.TypeToDto(z);
 				i++;
 			}
 
 			return list;
 		} catch (Exception e) {
 			API.createIssue("Service SQL exception in listTypes",
 					"A service SQL exception has encountered", e);
 			throw new RuntimeException(e);
 		}
 	}
 }
