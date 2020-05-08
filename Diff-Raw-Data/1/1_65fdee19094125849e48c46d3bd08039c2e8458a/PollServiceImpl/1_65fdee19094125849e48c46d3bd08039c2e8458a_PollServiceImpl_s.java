 package de.uniluebeck.itm.ep5.poll.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dozer.Mapper;
 import org.springframework.transaction.annotation.Transactional;
 
 import de.uniluebeck.itm.ep5.poll.domain.PollMapper;
 import de.uniluebeck.itm.ep5.poll.domain.boPoll;
 import de.uniluebeck.itm.ep5.poll.domain.xoPoll;
 import de.uniluebeck.itm.ep5.poll.repository.PollRepository;
 import de.uniluebeck.itm.ep5.util.Wildcard;
 
 public class PollServiceImpl implements PollService {
 
 
     private PollRepository pollRepository;
     private Mapper mapper;
 
     @Transactional
     @Override
     public void addPoll(xoPoll poll) {
         //boPoll b = mapper.map(poll, boPoll.class);
         boPoll b = PollMapper.createBO(poll);
         pollRepository.add(b);
     }
 
     @Transactional
     @Override
     public void updatePoll(xoPoll poll) {
         //boPoll b = mapper.map(poll, boPoll.class);
         boPoll b = PollMapper.createBO(poll);
         pollRepository.update(b);
     }
 
     @Transactional(readOnly = true)
     @Override
     public List<xoPoll> getPolls() {
         List<boPoll> all = pollRepository.findAll();
         List<xoPoll> trans = new ArrayList<xoPoll>();
         for (boPoll p : all){
             //xoPoll x = mapper.map(p, xoPoll.class);
             xoPoll x = PollMapper.createXO(p);
             trans.add(x);
         }
 
         return trans;
     }
 
     @Transactional(readOnly = true)
     @Override
     public List<xoPoll> search(String search) {
         List<xoPoll> list = new ArrayList<xoPoll>();
         Wildcard wildcard = new Wildcard(search);
         for (xoPoll p : this.getPolls()) {
             if (wildcard.match(p.getTitle(), false)) {
                 list.add(p);
             }
         }
         return list;
     }
 
     /**
      * Used by Spring to inject the PollRepository.
      * @param pollRepository
      */
     public void setPollRepository(PollRepository pollRepository) {
         this.pollRepository = pollRepository;
     }
     /**
      * Used by Spring to inject the Mapper.
      * @param mapper
      */
     public void setMapper(Mapper mapper) {
         this.mapper = mapper;
     }
 }
