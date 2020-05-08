 package net.hetimatan.net.http.task.client;
 
 
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 
 import net.hetimatan.net.http.HttpGet;
 import net.hetimatan.util.event.EventTask;
 import net.hetimatan.util.event.EventTaskRunner;
 import net.hetimatan.util.event.net.KyoroSocketEventRunner;
 
 public class HttpGetReadBodyTask extends EventTask {
 
 	public static final String TAG = "HttpGetReadBodyTask";
 	private WeakReference<HttpGet> mOwner = null;
 	private EventTask mLast = null;
 	private boolean mHeaderIsReadable = false;
 	private boolean mIsKeep = false;
 
 	public HttpGetReadBodyTask(HttpGet client, EventTask last) {
 		mOwner = new WeakReference<HttpGet>(client);
 		mLast = last;
 		errorAction(last);
 	}
 
 	@Override
 	public String toString() {
 		return TAG;
 	}
 
 	@Override
 	public boolean isKeep() {
 		return mIsKeep;
 	}
 
 	//
 	@Override
 	public void action(EventTaskRunner runner) throws IOException, InterruptedException {
 		HttpGet httpGet = mOwner.get();
 		if(!httpGet.bodyIsReadeable()) {
 			mIsKeep = true;
 			return;
 		} else {
 			mIsKeep = false;
 		}
 
 		httpGet.recvBody();
 		if(httpGet.isRedirect()) {
 			httpGet.update(httpGet.getLocation());
 			httpGet.startTask((KyoroSocketEventRunner)runner, mLast);
 		} else {
 //			System.out.println("-------------------------------------------------------------------------------------------");
 			nextAction(mLast);
 		}
 	}
 	
 }
