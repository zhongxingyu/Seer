 package baijie.test;
 
 import java.util.ArrayList;
 
 import util.webpage.Constant;
 import util.webpage.Course;
 import util.webpage.Post;
 import util.webpage.ReadPageHelper;
 import util.webpage.SchoolWebpageParser;
 import util.webpage.SchoolWebpageParser.ParserListener;
 import util.webpage.SchoolWebpageParser.ParserListenerAdapter;
 
 
 public class Test {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try{
		switch(1){
 		case 1:
 			SchoolWebpageParser parser1 = new SchoolWebpageParser();
 			parser1.setUser("20106173", "20106173");
			ArrayList<Course> result1 = parser1.parseScores(Constant.url.已选下学期课程);
 			if(result1.isEmpty())
 				System.out.println("result1 is empty!");
 			else
 				System.out.println(result1);
 		break;
 		case 2:
 			ParserListener listener2 = new ParserListenerAdapter(){
 
 				/* (non-Javadoc)
 				 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onWarn(int, java.lang.String)
 				 */
 				@Override
 				public void onWarn(int code, String message) {
 					System.err.println("WARN "+code+": "+message);
 				}
 
 				/* (non-Javadoc)
 				 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onInformation(int, java.lang.String)
 				 */
 				@Override
 				public void onInformation(int code, String message) {
 					System.out.println("INFO "+code+": "+message);
 				}
 				
 			};
 			SchoolWebpageParser parser2 = new SchoolWebpageParser(listener2, "20106173", "20106173");
 			System.out.println(parser2.parseScores(Constant.url.个人全部成绩));
 		break;
 		case 3:
 			SchoolWebpageParser parser3 = new SchoolWebpageParser(new MyListener(), "20106173", "20106173");
 			ArrayList<Course> result3 = parser3.parseScores(Constant.url.个人全部成绩);
 			for(Course course3:result3)
 				System.out.println(course3.getGradePoint() + "\t" + course3.toString());
 		break;
 		case 4:
 			SchoolWebpageParser parser4 = new SchoolWebpageParser(new MyListener());
 			ArrayList<Post> result4 = parser4.parsePosts(Post.SOURCES.STUDENT_WEBSITE_OF_SCCE, null, null, 10);
 			for(Post post4:result4)
 				System.out.println(parser4.parsePostMainBody(post4)+"\n");
 			System.out.println(parser4.parsePostMainBody(new Post()));
 		break;
 		case 5:
 			SchoolWebpageParser parser5 = new SchoolWebpageParser(new MyListener());
 			MyOnReadPageHelper onReadPageHelper = new MyOnReadPageHelper();
 			parser5.setOnReadPageListener(onReadPageHelper);
 			String[] categories = new String[]{Post.CATEGORYS.IN_SCCE[4],Post.CATEGORYS.IN_TEACHING_AFFAIRS_WEBSITE[4],
 					Post.CATEGORYS.IN_STUDENT_WEBSITE_OF_SCCE[4]};
 			ArrayList<Post> result5 = null;
 //			result5 = parser5.parsePostsFromSCCEStudent(Post.CATEGORYS.IN_STUDENT_WEBSITE_OF_SCCE[5], null, null, 1);
 			result5 = parser5.parsePosts(null, null, -1); 
 //			for(Post post:result5)
 //				System.out.println(post);
 			System.out.println("共 "+result5.size()+" 条， "+onReadPageHelper.pageNumber+" 页 "+onReadPageHelper.totalSize/1024.0/1024+" MB");
 		break;
 		default:;
 		}
 		}catch(Exception e){
 			System.err.println("------------------Start catch------------------");
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 			System.err.println("------------------End catch------------------");
 		}
 	}
 	
 	private static class MyOnReadPageHelper implements ReadPageHelper.OnReadPageListener{
 		int totalSize = 0;
 		int pageNumber = 0;
 
 		@Override
 		public void onGet(String url, int statusCode, String statusMessage,
 				int pageSize) {
 			System.out.println("URL: "+url+"\nStatus Code: "+statusCode+"\tStatusMessage: "
 					+statusMessage+"\t Page Size: "+pageSize);
 			totalSize+=pageSize;
 			pageNumber++;
 		}
 		
 	}
 	private static class MyListener extends ParserListenerAdapter{
 		public MyListener(){
 			super();
 		}
 
 		/* (non-Javadoc)
 		 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onWarn(int, java.lang.String)
 		 */
 		@Override
 		public void onWarn(int code, String message) {
 			System.err.println("WARN "+code+": "+message);
 		}
 
 		/* (non-Javadoc)
 		 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onInformation(int, java.lang.String)
 		 */
 		@Override
 		public void onInformation(int code, String message) {
 			System.out.println("INFO "+code+": "+message);
 		}
 		
 	}
 }
