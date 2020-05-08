 /*
  * CallbackTracker.java
  *
  * Wait for a particular tag to finish...
  *
  * You must follow the coding standards distributed
  * on the class web page.
  *
  * (C) 2011 Mike Dahlin
  *
  */
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Vector;
 import java.util.concurrent.locks.Condition;
 
 public class CallbackTracker implements DiskCallback{
 	
 	
 	
 	SimpleLock lock;
 	HashMap<Integer, DiskResult> list;
 	ArrayList<Integer> dontWaits;
 	Condition tagBroadcaster;
 	
 	public CallbackTracker() {
 		lock = new SimpleLock();
 		list = new HashMap<Integer, DiskResult>();
 		dontWaits = new ArrayList<Integer>();
 		tagBroadcaster = lock.newCondition();
 	}
 	
 	
     public void requestDone(DiskResult result){
     	lock.lock();
     	
     	if(dontWaits.contains(result.getTag())) {
     		dontWaits.remove(dontWaits.indexOf(result.getTag()));
     		lock.unlock();
     		return;
     	}
     	
     	
         // save result
     	list.put(result.getTag(), result);
     	// broadcast
     	tagBroadcaster.signalAll();
     	
     	
     	lock.unlock();
     }
 
     //
     // Wait for one tag to be done
     //
     public DiskResult waitForTag(int tag) {
     	lock.lock();
         // check for tag in all list
     	while(!list.containsKey(tag)) {
     		// if not there wait
     		try {
 				tagBroadcaster.await();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
     	}
     	
     	DiskResult ret = list.get(tag);
     	// if there: remove for list, return result
     	list.remove(tag);
     	lock.unlock();
         return ret;
     }
 
     //
     // Wait for a set of tags to be done
     //
     public Vector<DiskResult> waitForTags(Vector<Integer> tags){
     	lock.lock();
     	Iterator<Integer> iter = tags.iterator();
     	Vector<DiskResult> ret = new Vector<DiskResult>();
     	int ctag;
     	while(iter.hasNext()) {
     		ctag = iter.next();
     		while(!list.containsKey(ctag)) {
         		// if not there wait
         		try {
     				tagBroadcaster.await();
     			} catch (InterruptedException e) {
     				e.printStackTrace();
     			}
         	}
    		ret.add(list.get(ctag));
    		list.remove(ctag);
     	}
     	lock.unlock();
         return ret;
     }
 
     //
     // To avoid memory leaks, need to tell CallbackTracker
     // if there are tags that we don't plan to wait for.
     // When these results arrive, drop them on the
     // floor.
     //
     public void dontWaitForTag(int tag){
     	lock.lock();
     	
     	if(list.containsKey(tag)) { //if it's already in the list, remove it.
     		list.remove(tag);
     		lock.unlock();
     		return;
     	}
     	
     	dontWaits.add(tag); //add tag to a list of tags not to be waited for in the future
     	lock.unlock();
     }
 
     public void dontWaitForTags(Vector<Integer> tags){
     	lock.lock();
     	Iterator<Integer> iter = tags.iterator();
     	int ctag;
     	while(iter.hasNext()) {
     		ctag = iter.next();
     		if(list.containsKey(ctag)) {
     			list.remove(ctag);		//if it's already in the list, remove it.
     		} else {
     			dontWaits.add(ctag); //add tag to a list of tags not to be waited for in the future
     		}
     	}
     	lock.unlock();
     }
     
     public static void unit(Tester t) throws IllegalArgumentException, IOException {
     	t.set_object("CallbackTracker");
     	CallbackTracker cbt1 = new CallbackTracker();
     	Disk d1 = new Disk(cbt1);
     	
     	// request done
     	t.set_method("requestDone()");
     	int tag = 33;
     	byte[] buffer1 = new byte[Disk.SECTOR_SIZE];
     	DiskResult dr1 = new DiskResult(Disk.READ, tag, 2000, buffer1);
     	cbt1.requestDone(dr1);
     	t.is_true(cbt1.list.containsKey(tag));
     	t.is_equal(1, cbt1.list.size());
     	
     	tag = 34;
     	DiskResult dr2 = new DiskResult(Disk.READ, tag, 2000, buffer1);
     	cbt1.requestDone(dr2);
     	t.is_true(cbt1.list.containsKey(tag));
     	t.is_equal(2, cbt1.list.size());
     	
     	tag = 35;
     	DiskResult dr3 = new DiskResult(Disk.READ, tag, 2000, buffer1);
     	cbt1.requestDone(dr3);
     	t.is_true(cbt1.list.containsKey(tag));
     	t.is_equal(3, cbt1.list.size());
     	
     	
     	
     	// dontWaitForTag
     	t.set_method("dontWaitForTag()");
     	
     	cbt1.dontWaitForTag(33);
     	t.is_equal(2, cbt1.list.size());
     	t.is_equal(0, cbt1.dontWaits.size());
     	t.is_true(cbt1.list.containsKey(34));
     	t.is_true(cbt1.list.containsKey(35));
     	
     	cbt1.dontWaitForTag(33);
     	t.is_equal(2, cbt1.list.size());
     	t.is_equal(1, cbt1.dontWaits.size());
     	t.is_true(cbt1.dontWaits.contains(33));
     	t.is_true(cbt1.list.containsKey(34));
     	t.is_true(cbt1.list.containsKey(35));
     	
     	cbt1.dontWaitForTag(32);
     	t.is_equal(2, cbt1.list.size());
     	t.is_equal(2, cbt1.dontWaits.size());
     	t.is_true(cbt1.dontWaits.contains(33));
     	t.is_true(cbt1.dontWaits.contains(32));
     	t.is_true(cbt1.list.containsKey(34));
     	t.is_true(cbt1.list.containsKey(35));
     	
     	cbt1.dontWaitForTag(34);
     	t.is_equal(1, cbt1.list.size());
     	t.is_equal(2, cbt1.dontWaits.size());
     	t.is_true(cbt1.dontWaits.contains(33));
     	t.is_true(cbt1.dontWaits.contains(32));
     	t.is_true(cbt1.list.containsKey(35));
     	
     	cbt1.dontWaitForTag(35);
     	t.is_equal(0, cbt1.list.size());
     	t.is_equal(2, cbt1.dontWaits.size());
     	t.is_true(cbt1.dontWaits.contains(33));
     	t.is_true(cbt1.dontWaits.contains(32));
     	
     	DiskResult dr4 = new DiskResult(Disk.READ, 33, 2000, buffer1);
     	cbt1.requestDone(dr4);
     	t.is_equal(0, cbt1.list.size());
     	t.is_equal(1, cbt1.dontWaits.size());
     	t.is_true(cbt1.dontWaits.contains(32));
     	
     	DiskResult dr5 = new DiskResult(Disk.READ, 32, 2000, buffer1);
     	cbt1.requestDone(dr5);
     	t.is_equal(0, cbt1.list.size());
     	t.is_equal(0, cbt1.dontWaits.size());
     	
     	
     	
     	
     	// dontWaitForTags(Vector)
     	t.set_method("dontWaitForTag(Vector)");
     	Vector<Integer> v1 = new Vector<Integer>();
     	v1.add(100);
     	v1.add(101);
     	v1.add(102);
     	
     	cbt1.dontWaitForTags(v1);
     	t.is_equal(3, cbt1.dontWaits.size());
     	t.is_true(cbt1.dontWaits.contains(100));
     	t.is_true(cbt1.dontWaits.contains(101));
     	t.is_true(cbt1.dontWaits.contains(102));
     	
     	v1 = new Vector<Integer>();
     	v1.add(103);
     	v1.add(104);
     	v1.add(105);
     	
     	cbt1.dontWaitForTags(v1);
     	t.is_equal(6, cbt1.dontWaits.size());
     	t.is_true(cbt1.dontWaits.contains(103));
     	t.is_true(cbt1.dontWaits.contains(104));
     	t.is_true(cbt1.dontWaits.contains(105));
     	
     	
     	
     	
     	//wait for Tag
     	t.set_method("waitForTag()");
     	d1.startRequest(Disk.READ, 200, 555, buffer1);
     	cbt1.waitForTag(200);
     	t.is_equal(0, cbt1.list.size());
     	
     	d1.startRequest(Disk.READ, 201, 555, buffer1);
     	cbt1.waitForTag(201);
     	t.is_equal(0, cbt1.list.size());
     	
     	d1.startRequest(Disk.READ, 202, 555, buffer1);
     	cbt1.waitForTag(202);
     	t.is_equal(0, cbt1.list.size());
     	
     	
     	
     	
     	// waitForTag(Vector)
     	t.set_method("waitForTag(Vector)");
     	v1 = new Vector<Integer>();
     	v1.add(300);
     	v1.add(301);
     	v1.add(302);
     	d1.startRequest(Disk.READ, 300, 555, buffer1);
     	d1.startRequest(Disk.READ, 301, 555, buffer1);
     	d1.startRequest(Disk.READ, 302, 555, buffer1);
     	cbt1.waitForTags(v1);
     	
     	
     }
 }
