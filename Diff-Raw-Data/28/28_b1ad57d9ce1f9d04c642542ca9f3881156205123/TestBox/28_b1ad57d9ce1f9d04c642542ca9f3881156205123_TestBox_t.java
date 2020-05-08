 package test;
 
 import com.zhuri.slot.SlotTimer;
 import com.zhuri.slot.SlotThread;
 
 public class TestBox {
 
 	static SlotTimer mTimer = new SlotTimer() {
 		public void invoke() {
 			System.out.println("start https test");
 
 			try {
 				TestTalkClient client = new TestTalkClient();
 				client.start();
 			} catch (Exception e) {
 				e.printStackTrace();
 				return;
 			}
 		}
 	};
 
 	public static void main(String args[]) {
 		WebCrawler crawler = null;
 
 		try {
 			SlotThread.Init();
 
			for (String url: args)
				new WebCrawler(url).start();
 
 			mTimer.reset(10000);
 			while (SlotThread.step());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 }
