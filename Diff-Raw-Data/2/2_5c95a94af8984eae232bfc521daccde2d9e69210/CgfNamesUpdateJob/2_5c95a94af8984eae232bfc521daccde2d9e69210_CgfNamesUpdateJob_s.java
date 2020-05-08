 package com.laurinka.skga.server.job;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.ejb.Schedule;
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.TypedQuery;
 
 import com.laurinka.skga.server.model.CgfNumber;
 import com.laurinka.skga.server.model.Result;
 import com.laurinka.skga.server.repository.ConfigurationRepository;
 import com.laurinka.skga.server.scratch.CgfGolferNumber;
 import com.laurinka.skga.server.services.WebsiteService;
 import com.laurinka.skga.server.utils.Utils;
 
 @Stateless
 public class CgfNamesUpdateJob {
 
 	@Inject
 	private EntityManager em;
 
 	@Inject
 	Logger log;
 	@Inject
 	ConfigurationRepository config;
 	@Inject
 	WebsiteService service;
 
 	@SuppressWarnings("UnusedDeclaration")
	@Schedule(persistent = false, hour = "*", minute = "*/5")
 	public void fixNames() throws IOException {
 		TypedQuery<CgfNumber> questionQuery = em.createQuery(
 				"select m from CgfNumber m where m.name2 like '%\\?%'",
 				CgfNumber.class);
 		questionQuery.setMaxResults(10);
 		List<CgfNumber> resultList = questionQuery.getResultList();
 		if (null == resultList || resultList.isEmpty()) {
 			log.info("No corrupted names need to be update. ");
 			return;
 		}
 		for (CgfNumber s : resultList) {
 			process(s);
 		}
 	}
 
 	private void process(CgfNumber s) {
 		CgfGolferNumber nr = new CgfGolferNumber(s.getNr());
 		Result detail = service.findDetail(nr);
 		log.info("Checking " + nr.asString() + " ");
 		if (null == detail) {
 			return;
 		}
 		log.info("Updating with " + detail.toString());
 		s.setDate(new Date());
 		s.setName(detail.getName());
 		s.setName2(Utils.stripAccents(detail.getName()));
 		em.merge(s);
 	}
 }
