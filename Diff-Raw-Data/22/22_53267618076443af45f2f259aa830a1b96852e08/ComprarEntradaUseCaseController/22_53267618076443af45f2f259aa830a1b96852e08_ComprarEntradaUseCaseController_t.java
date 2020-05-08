 package com.Shows.Domain.Controllers;
 
 import java.sql.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.Session;
 
 import com.Shows.HibernateUtil;
 import com.Shows.Data.Controllers.ControllerDataFactory;
 import com.Shows.Data.Interfaces.IControllerEspectacle;
 import com.Shows.Data.Interfaces.IControllerRepresentacio;
 import com.Shows.Data.Interfaces.IControllerSeientsEnRepresentacio;
 import com.Shows.Domain.Adapters.AdapterFactory;
 import com.Shows.Domain.Adapters.IConversorAdapter;
 import com.Shows.Domain.Adapters.IPagamentAdapter;
 import com.Shows.Domain.Exceptions.NoHiHaRepresentacions;
 import com.Shows.Domain.Exceptions.PagamentNoAutoritzat;
 import com.Shows.Domain.Exceptions.SeientsNoDisp;
 import com.Shows.Domain.Exceptions.SeientsNoOk;
 import com.Shows.Domain.Exceptions.ServeiNoDisponible;
 import com.Shows.Domain.Model.Entrada;
 import com.Shows.Domain.Model.Espectacle;
 import com.Shows.Domain.Model.Moneda;
 import com.Shows.Domain.Model.Representacio;
 import com.Shows.Domain.Model.SeientEnRepresentacio;
 import com.Shows.Domain.Model.ShowsCom;
 import com.Shows.Domain.Model.TipusSessio;
 import com.Shows.TupleTypes.DadesEntrada;
 import com.Shows.TupleTypes.DadesRepresentacio;
 import com.Shows.TupleTypes.PosicioSeient;
 
 public class ComprarEntradaUseCaseController {
 
 	private Set<PosicioSeient> seients;
 	private float preuTotal;
 	boolean preuAssignat;
 	private ControllerDataFactory controllerDataFactory = ControllerDataFactory
 			.getInstance();
 
 	private ConsultaRepresentacioUseCaseController consultaRepresentacioUseCaseController;
 	private ConsultaOcupacioUseCaseController consultaOcupacioUseCaseController;
 
 	public void init() {
 		seients = new HashSet<PosicioSeient>();
 		preuTotal = 0;
 		preuAssignat = false;
 	}
 
 	public Set<String> obteEspectacles() {
 		IControllerEspectacle controllerEspectacle = controllerDataFactory
 				.getControllerEspectacle();
 		List<Espectacle> espectacles = controllerEspectacle.allEspectacles();
 		Set<String> titolEspectacles = new HashSet<String>(espectacles.size());
 
 		for (Espectacle espectacle : espectacles) {
 			titolEspectacles.add(espectacle.getTitol());
 		}
 		return titolEspectacles;
 	}
 
 	public Set<DadesRepresentacio> obteRepresentacions(final String titol,
 			final Date data) throws NoHiHaRepresentacions {
 
 		consultaRepresentacioUseCaseController = new ConsultaRepresentacioUseCaseController();
 		return consultaRepresentacioUseCaseController.obteRepresentacions(
 				titol, data);
 	}
 
 	public Set<PosicioSeient> obteOcupacio(final String nomLocal,
 			final TipusSessio sessio, final int nombEspectadors)
 			throws SeientsNoDisp {
 
 		consultaOcupacioUseCaseController = new ConsultaOcupacioUseCaseController();
 
 		return consultaOcupacioUseCaseController.obteOcupacio(nomLocal, sessio,
 				nombEspectadors,
 				consultaRepresentacioUseCaseController.getData());
 	}
 
 	public DadesEntrada selecionarSeients(final Set<PosicioSeient> seients)
 			throws SeientsNoOk {
 		if (seients.size() != consultaOcupacioUseCaseController
 				.getNombEspectadors())
 			throw new SeientsNoOk(
 					"El nombre de seients no s igual al nombre d'espectadors de l'entrada");
 
 		this.seients = seients;
 
 		IControllerRepresentacio controllerRepresentacio = controllerDataFactory
 				.getControllerRepresentacio();
 
 		Representacio representacio = controllerRepresentacio.getRepresentacio(
 				consultaOcupacioUseCaseController.getNomLocal(),
 				consultaOcupacioUseCaseController.getSessio(),
 				consultaRepresentacioUseCaseController.getData());
 
 		ShowsCom showsCom = ShowsCom.getInstance();
 
 		float comisio = showsCom.getComissio();
 		Set<String> canvis = showsCom.getCanvis();
 
 		canvis.add(showsCom.getDivisa().toString());

		float preuTotal = representacio.obtePreu() + comisio;
 		DadesEntrada dadesEntrada = new DadesEntrada(preuTotal
				* consultaOcupacioUseCaseController.getNombEspectadors(),
				canvis);
 
		this.preuTotal = preuTotal
				* consultaOcupacioUseCaseController.getNombEspectadors();
 
 		return dadesEntrada;
 	}
 
 	public float obtePreuMoneda(final String moneda) throws ServeiNoDisponible {
 
 		AdapterFactory adapterFactory = AdapterFactory.getInstance();
 
 		IConversorAdapter conversorAdapter = adapterFactory
 				.getConversorAdapter();
 
 		Moneda divisa = ShowsCom.getInstance().getDivisa();
 
 		double rate = conversorAdapter.convert(divisa, Moneda.valueOf(moneda));
 
 		return (float) (this.preuTotal * rate);
 	}
 
 	public void pagament(final String dni, final int codiB,
 			final String numCompte) throws PagamentNoAutoritzat {
 		IPagamentAdapter pagamentAdapter = AdapterFactory.getInstance()
 				.getPagamentAdapter();
 
 		int codiBancShows = ShowsCom.getInstance().getCodiBanc();
 		String numcompteShows = ShowsCom.getInstance().getNumeroCompte();
 
 		pagamentAdapter.autoritza(dni, codiB, numCompte, this.preuTotal,
 				codiBancShows, numcompteShows);
 
 		IControllerRepresentacio controllerRepresentacio = ControllerDataFactory
 				.getInstance().getControllerRepresentacio();
 
 		Representacio representacio = controllerRepresentacio.getRepresentacio(
 				consultaOcupacioUseCaseController.getNomLocal(),
 				consultaOcupacioUseCaseController.getSessio(),
 				consultaRepresentacioUseCaseController.getData());
 
 		Entrada entrada = representacio.createEntrada(
 				consultaRepresentacioUseCaseController.getTitol(), dni,
 				consultaOcupacioUseCaseController.getNombEspectadors(),
 				consultaRepresentacioUseCaseController.getData(),
 				this.preuTotal);
 
 		Session session = HibernateUtil.getSession();
 		session.beginTransaction();
 
 		session.saveOrUpdate(entrada);
 
 		IControllerSeientsEnRepresentacio controllerSeientsEnRepresentacio = ControllerDataFactory
 				.getInstance().getControllerSeientsEnRepresentacio();
 
 		Iterator<PosicioSeient> iterator = seients.iterator();
 		HashSet<SeientEnRepresentacio> seientsEnRepresentacio = new HashSet<SeientEnRepresentacio>();
 
 		while (iterator.hasNext()) {
 			PosicioSeient aux = iterator.next();
 			SeientEnRepresentacio seientEnRepresentacio = controllerSeientsEnRepresentacio
 					.getSeientEnRepresentacio(representacio
 							.getAuxiliarRepresentacio().getLocal().getNom(),
 							aux.getFila(), aux.getColumna(), representacio
 									.getAuxiliarRepresentacio().getData(),
 							representacio.getAuxiliarRepresentacio()
 									.getSessio().getSessio());
 
 			seientEnRepresentacio.ocuparSeient();
 
 			session.saveOrUpdate(seientEnRepresentacio);
 
 			seientsEnRepresentacio.add(seientEnRepresentacio);
 		}
 
 		representacio.setNombreSeientsLliures(representacio
 				.getNombreSeientsLliures() - seients.size());
 		session.merge(representacio);
 
 		session.getTransaction().commit();
 	}
 }
