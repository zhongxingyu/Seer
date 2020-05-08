 package burst.reader.service;
 
 import java.sql.SQLException;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.transaction.annotation.Transactional;
 
 import burst.reader.dto.BookMarkDTO;
 
 public class BookMarkMonitorService implements Runnable {
 
     public static final int QUEUE_SIZE = 100;
 
 	private BlockingQueue<BookMarkDTO> q;
     private String userAgentFilter;
 
     public void push(BookMarkDTO bookMark) {
 		q.offer(bookMark);
 	}
 	
 	public BookMarkMonitorService() {
 		q = new ArrayBlockingQueue<BookMarkDTO>(QUEUE_SIZE);
         new Thread(this).start();
 	}
 	
 	private BookMarkService bookMarkService;
 
 	public void setBookMarkService(BookMarkService bookMarkService) {
 		this.bookMarkService = bookMarkService;
 	}
 
 	public BookMarkService getBookMarkService() {
 		return bookMarkService;
 	}
 	
 	@Override
 	public void run() {
 
         boolean useFilter = !"".equals(userAgentFilter);
 
 		while(true) {
             BookMarkDTO bookMark;
 			try {
                 bookMark = q.take();
 			} catch (InterruptedException e) {
 				continue;
 			}
 			try {
                 if(useFilter) {
                    if(bookMark.getUserAgent() != null) {
                        if(bookMark.getUserAgent().matches(userAgentFilter)) {
                            continue;
                        }
                     }
                 }
 				bookMarkService.addBookMarkWithAuto(bookMark);
 			}
 			catch (SQLException ex) { }
 		}
 	}
 
     public void setUserAgentFilter(String userAgentFilter) {
         this.userAgentFilter = userAgentFilter;
     }
 }
