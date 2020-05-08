 /**
  * @package weibo4j.dev.timeline
  * @file GetUserTimeline.java
  * @author lzrak47
  * @date 2013-3-13
  */
 package weibo4j.dev.timeline;
 
 import weibo4j.Timeline;
 import weibo4j.model.Status;
 import weibo4j.model.StatusWapper;
 import weibo4j.model.Paging;
 import weibo4j.model.WeiboException;
 
 public class GetUserTimeline {
 
 	public static void main(String[] args) {
 		String access_token = args[0];
 		Timeline tm = new Timeline();
 		tm.client.setToken(access_token);
 		try {
            for(int i = 1 ;; i++)
            {
                StatusWapper status = tm.getUserTimelineByUid(args[1], new Paging(i,200), 0 , 0);
                if (status.getStatuses().size() == 0)
                    break;
                for(Status s : status.getStatuses()){
                    System.out.println(s.getText());
                }
            }
 		} catch (WeiboException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
