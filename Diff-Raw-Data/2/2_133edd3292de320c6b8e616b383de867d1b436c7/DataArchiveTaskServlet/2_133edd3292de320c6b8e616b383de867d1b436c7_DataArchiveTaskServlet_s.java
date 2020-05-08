 package org.vsegda.servlet;
 
 import com.google.appengine.api.taskqueue.Queue;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.TaskOptions;
 import org.vsegda.dao.DataStreamDAO;
 import org.vsegda.dao.Factory;
 import org.vsegda.data.DataArchive;
 import org.vsegda.data.DataItem;
 import org.vsegda.data.DataStream;
 import org.vsegda.data.DataStreamMode;
 import org.vsegda.util.TimeUtil;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.logging.Logger;
 
 /**
  * @author Roman Elizarov
  */
 public class DataArchiveTaskServlet extends HttpServlet {
     private static final Logger log = Logger.getLogger(DataArchiveTaskServlet.class.getName());
 
     public static void enqueueDataArchiveTask(long streamId) {
         log.info("Enqueueing data archive task for streamId=" + streamId);
         Queue queue = QueueFactory.getQueue("archiveTaskQueue");
         queue.add(TaskOptions.Builder
                 .withUrl("/task/dataArchive")
                 .param("id", String.valueOf(streamId)));
     }
 
     @SuppressWarnings({"unchecked"})
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         Long streamId = Long.parseLong(req.getParameter("id"));
         log.info("Archiving data for streamId=" + streamId);
         PersistenceManager pm = Factory.getPersistenceManager();
         try {
             DataStream stream = pm.getObjectById(DataStream.class, streamId);
             if (stream.getMode() == DataStreamMode.LAST) {
                 log.warning("Stream mode is " + stream.getMode() + ", ignoring task");
                 return;
             }
             DataItem firstItem = DataStreamDAO.getOrFindFirstItem(pm, stream);
             if (firstItem == null || firstItem.isRecent()) {
                 log.info("First stream item is recent or missing: " + firstItem);
                 return;
             }
             // Archive up to next midnight
             Calendar cal = Calendar.getInstance(TimeUtil.TIMEZONE);
             cal.setTimeInMillis(firstItem.getTimeMillis());
             cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MILLISECOND));
             cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
             cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
             Query query = pm.newQuery(DataItem.class);
             query.setOrdering("timeMillis asc");
             query.declareParameters("long id, long limit");
             query.setFilter("streamId == id && timeMillis <= limit");
             Collection<DataItem> items = (Collection<DataItem>) query.execute(streamId, cal.getTimeInMillis());
             if (items.isEmpty()) {
                 log.warning("No items to archive");
                 return;
             }
             if (stream.getMode() == DataStreamMode.RECENT) {
                 log.info("Only recent items are kept, removing old ones");
                 pm.deletePersistentAll(items);
                 return;
             }
             // Actually archive
             DataArchive archive = new DataArchive(streamId);
             archive.setItems(items);
             log.info("Creating archive " + archive);
             pm.makePersistent(archive);
             stream.setFirstItemKey(null);
             pm.deletePersistentAll(items);
             // create next task to check this stream
             enqueueDataArchiveTask(streamId);
         } finally {
             pm.close();
         }
     }
 }
