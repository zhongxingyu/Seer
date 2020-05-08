 package org.emsionline.emsiweb.service.jpa;
 
 import java.util.List;
 
 import org.emsionline.emsiweb.domain.Church;
 import org.emsionline.emsiweb.repository.ChurchRepository;
 import org.emsionline.emsiweb.service.ChurchService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;
 import org.springframework.stereotype.Repository;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.google.common.collect.Lists;
 
//@Service("jpaChurchService")
//@Repository
 @Transactional
 public class ChurchServiceImpl implements ChurchService {
 
 
 	
 	@Autowired
 	private ChurchRepository churchRepository;
 	
 	
 	@Transactional(readOnly = true)
 	public List<Church> findAll() {
 		return Lists.newArrayList(churchRepository.findAll());
 	}
 
 	@Transactional(readOnly = true)
 	public Church findById(Long id) {
 		return churchRepository.findOne(id);
 	}
 
 	public Church save(Church church) {
 		return churchRepository.save(church);
 	}
 
 	@Transactional(readOnly = true)
 	public Page<Church> findAllByPage(Pageable pageable) {
 		return churchRepository.findAll(pageable);
 	}
 
 }
