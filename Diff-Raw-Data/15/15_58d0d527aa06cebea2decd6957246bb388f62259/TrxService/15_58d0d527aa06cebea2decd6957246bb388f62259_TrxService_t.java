 package id.web.martinusadyh.logbook.service;
 
import id.web.martinusadyh.logbook.domain.trx.LogBookHeader;
import java.util.List;

 /**
  *
  * @author martinus
  */
 public interface TrxService {
     
    public List<LogBookHeader> findAllLogBook(Integer first, Integer pageSize);

    public Long countLogBookHeader();
    
 }
