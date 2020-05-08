 package org.bonitasoft.demo.callforpaper.service;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.TypedQuery;
 
 import org.bonitasoft.callforpaper.boslib.ProcessHandler;
 import org.bonitasoft.demo.callforpaper.model.Paper;
 import org.bonitasoft.demo.callforpaper.model.PaperState;
 
 @Stateless
 public class PaperService implements Serializable {
 
 	private static final long serialVersionUID = -526852528138167511L;
 
 	private static final String QUERY_ALL = "Paper.all";
 
 	private static final String QUERY_BY_STATUS = "Paper.bystate";
 
 	@Inject
 	private EntityManager em;
 
 	public Paper createPaper(Paper paper) {
 		paper.setCreationDate(new Date());
 		paper.setState(PaperState.NEW);
         em.persist(paper);
         try {
             ProcessHandler.getProcessHandler("Paper_validation", "1.0").startProcess(paper.getSubmitterEmail(), paper.getId());
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return paper;
 	}
 
    public Paper createPaperWithoutStartingProcess(Paper paper) {
        paper.setCreationDate(new Date());
        paper.setState(PaperState.NEW);
        em.persist(paper);

        return paper;
    }

 	public Paper updatePaper(Paper paper) {
 		em.merge(paper);
 		return paper;
 	}
 
 	public Paper getPaper(Long id) {
 		return em.find(Paper.class, id);
 	}
 
 	public List<Paper> getAllPapers() {
 		TypedQuery<Paper> q = em.createNamedQuery(QUERY_ALL, Paper.class);
 		return q.getResultList();
 	}
 
 	public List<Paper> getPapersByState(String state) {
 		TypedQuery<Paper> q = em.createNamedQuery(QUERY_BY_STATUS, Paper.class);
 		q.setParameter("state", PaperState.valueOf(state));
 		return q.getResultList();
 	}
 
 	public void removePaper(Long id) {
 		em.remove(em.find(Paper.class, id));
 	}
 
 	public void removePaper(Paper paper) {
 		em.remove(em.merge(paper));
 	}
 
 }
