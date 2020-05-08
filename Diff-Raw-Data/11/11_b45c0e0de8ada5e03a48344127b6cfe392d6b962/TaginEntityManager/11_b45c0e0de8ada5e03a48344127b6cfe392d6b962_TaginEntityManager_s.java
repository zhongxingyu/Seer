 package ca.idrc.tagin.dao;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import ca.idrc.tagin.model.Beacon;
 import ca.idrc.tagin.model.Fingerprint;
 import ca.idrc.tagin.model.Neighbour;
 import ca.idrc.tagin.model.Pattern;
 import ca.idrc.tagin.model.URN;
 import ca.idrc.tagin.spi.v1.URNManager;
 
 public class TaginEntityManager implements TaginDao {
 
 	private EntityManager mEntityManager;
 
 	public TaginEntityManager() {
 		mEntityManager = EMFService.createEntityManager();
 	}
 
 	@Override
 	public String persistPattern(Pattern pattern) {
 		Fingerprint fp = new Fingerprint(pattern);
 		URNManager.generateURN(this, fp);
 		return fp.getUrn();
 	}
 
 	@Override
 	public void persistFingerprint(Fingerprint fp) {
 		mEntityManager.getTransaction().begin();
 		mEntityManager.persist(fp);
 		mEntityManager.flush();
 		fp.getPattern().setId(fp.getPattern().getKey().getId());
 		mEntityManager.getTransaction().commit();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Pattern> listPatterns() {
 		Query query = mEntityManager.createQuery("select p from Pattern p");
 		List<Pattern> patterns = query.getResultList();
 		for (Pattern p : patterns) {
 			p.getBeacons(); // Forces eager-loading
 		}
 		return patterns;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Fingerprint> listFingerprints() {
 		Query query = mEntityManager.createQuery("select f from Fingerprint f");
 		List<Fingerprint> fingerprints = query.getResultList();
 		return fingerprints;
 	}
 	
 	@Override
 	public Pattern getPattern(Long id) {
 		Pattern p = findPattern(id);
 		if (p != null)
 			p.getBeacons(); // Forces eager-loading
 		return p;
 	}
 
 	@Override
 	public Fingerprint getFingerprint(Long id) {
 		Fingerprint fp = mEntityManager.find(Fingerprint.class, id);
 		if (fp != null)
 			fp.getPattern().getBeacons(); // Forces eager-loading
 		return fp;
 	}
 
 	@Override
 	public Fingerprint getFingerprint(String urn) {
 		return findFingerprint(urn);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Neighbour> getNeighbours(Fingerprint fp) {
 		List<Neighbour> neighbours = new ArrayList<Neighbour>();
 		for (Beacon b : fp.getPattern().getBeacons().values()) {
 			// TODO use this query instead when KEY() method is implemented in Google Datanucleus JPQL
 			//Query query = mEntityManager.createQuery("select p from Pattern p join p.beacons b where KEY(b) = :bId");
 			//query.setParameter("bId", "'" + b.getId() + "'");
 			Query query = mEntityManager.createQuery("select p from Pattern p");
 			List<Pattern> patterns = query.getResultList();
 
 			for (Pattern p : patterns) {
 				if (p.contains(b.getId())) {
 					Fingerprint f = mEntityManager.find(Fingerprint.class, p.getKey().getParent());
 					if (f.getId() != fp.getId()) {
 						neighbours.add(new Neighbour(f, fp.rankDistanceTo(f)));
 					}
 				}
 			}
 		}
 		return neighbours;
 	}
 	
 	@Override
 	public List<URN> fetchNumOfNeighbours(Fingerprint fp, Integer maxCount) {
 		Map<String,URN> neighbours = new LinkedHashMap<String,URN>();
 		for (Neighbour n : getNeighbours(fp)) {
 			String key = n.getFingerprint().getUrn();
 			if (key != null && !neighbours.containsKey(key))
 				neighbours.put(key, new URN(key));
 			if (neighbours.size() >= maxCount)
 				break;
 		}
 		
 		if (neighbours.size() >= maxCount || neighbours.size() == 0) {
 			return new ArrayList<URN>(neighbours.values());
 		} else {
			return fetchNumOfNeighboursAux(0, maxCount, neighbours);
 		}
 	}
 	
	private List<URN> fetchNumOfNeighboursAux(int index, Integer maxCount, Map<String, URN> neighbours) {
 		List<URN> urns = new ArrayList<URN>(neighbours.values());
 		Fingerprint fp = getFingerprint(urns.get(index).getValue());
 		index++;
 		
 		for (Neighbour n : getNeighbours(fp)) {
 			String key = n.getFingerprint().getUrn();
			if (key != null && !neighbours.containsKey(key))
 				neighbours.put(key, new URN(key));
 			if (neighbours.size() >= maxCount)
 				break;
 		}
 		
 		if (neighbours.size() >= maxCount || index == neighbours.size()) {
 			return new ArrayList<URN>(neighbours.values());
 		} else {
			return fetchNumOfNeighboursAux(index, maxCount, neighbours);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Pattern findPattern(Long id) {
 		Pattern p = null;
 		Query query = mEntityManager.createQuery("select p from Pattern p where p.id = " + id);
 		List<Pattern> result = query.getResultList();
 		if (result.size() > 0) {
 			p = result.get(0);
 		}
 		return p;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Fingerprint findFingerprint(String urn) {
 		Fingerprint fp = null;
 		Query query = mEntityManager.createQuery("select f from Fingerprint f where f.urn = '" + urn + "'");
 		List<Fingerprint> result = query.getResultList();
 		if (result.size() > 0) {
 			fp = result.get(0);
 		}
 		return fp;
 	}
 	
 	@Override
 	public void removePattern(Long id) {
 		Pattern p = findPattern(id);
 		if (p != null) {
 			Fingerprint parent = mEntityManager.find(Fingerprint.class, p.getKey().getParent());
 			mEntityManager.remove(parent);
 		}
 	}
 	
 	@Override
 	public void removeFingerprint(String urn) {
 		Fingerprint fp = findFingerprint(urn);
 		if (fp != null)
 			mEntityManager.remove(fp);
 	}
 
 	@Override
 	public void close() {
 		mEntityManager.close();
 	}
 	
 	@Override
 	public void beginTransaction() {
 		mEntityManager.getTransaction().begin();
 	}
 	
 	@Override
 	public void commitTransaction() {
 		mEntityManager.getTransaction().commit();
 	}
 
 }
