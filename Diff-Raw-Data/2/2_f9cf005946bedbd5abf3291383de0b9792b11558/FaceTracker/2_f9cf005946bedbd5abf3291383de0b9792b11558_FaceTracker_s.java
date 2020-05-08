 package org.on.puz.photobombsquad;
 
 import java.util.ArrayList;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.opencv.core.Mat;
 import org.opencv.core.MatOfRect;
 import org.opencv.core.Rect;
 import org.opencv.samples.facedetect.DetectionBasedTracker;
 
 public class FaceTracker {
 	private static class _FaceEntry {
 		public Rect rect;
 		public long earliestTimestamp;
 		public long latestTimestamp;
 	}
 	private static class _FaceList {
 		public ArrayList<_FaceEntry> faces = new ArrayList<_FaceEntry>();
 		public long latestTimestamp = 0;
 	}
 	private static class _MatEntry {
 		public final Mat m;
 		public final long timestampMs;
 		
 		_MatEntry(Mat _m,long _timestampMs) {
 			m = _m;
 			timestampMs = _timestampMs;
 		}
 	}
 	
 	private static final ExecutorService _executor = Executors.newSingleThreadExecutor();
 	
 	private final DetectionBasedTracker _classifier;
 	private final double _maxGrowthRate,_maxMoveRate;
 	private final long _faceDecayTime,_minNewFaceTime,_faceHoldTime;
 
 	private final ArrayList<_MatEntry> oldMats = new ArrayList<_MatEntry>();
 	
 	private final ArrayList<Rect> _good = new ArrayList<Rect>(),
 								  _bad  = new ArrayList<Rect>();
 	
 	private final _FaceList _faceList = new _FaceList();
 	
 	private Future<Rect[]> _job = null;
 	private long _jobTimestampMs = 0;
 	
 	private void _updateList(Rect[] faces,long timestampMs) {
 		_good.clear();
 		_bad.clear();
 		double dt = (timestampMs-_faceList.latestTimestamp)/1000.0;
 		ArrayList<_FaceEntry> newEntries = new ArrayList<_FaceEntry>();
 		boolean[] used = new boolean[faces.length];
 		for(_FaceEntry entry: _faceList.faces) {
 			if(timestampMs-entry.latestTimestamp > _faceDecayTime) {
 				continue;
 			}
 			for(int j=0;j<faces.length;++j) {
 				if(used[j]) {
 					continue;
 				}
 				double diffX = (faces[j].x+faces[j].width/2.0)-(entry.rect.x+entry.rect.width/2.0),
 					   diffY = (faces[j].y+faces[j].height/2.0)-(entry.rect.y+entry.rect.height/2.0);
 				if(Math.abs(faces[j].area()-entry.rect.area()) < _maxGrowthRate*dt &&
 				   Math.hypot(diffX, diffY) < _maxMoveRate*dt) {
 					used[j] = true;
 					entry.rect = faces[j];
 					entry.latestTimestamp = timestampMs;
 					newEntries.add(entry);
 					if(timestampMs-entry.earliestTimestamp > _minNewFaceTime) {
 						_good.add(faces[j]);
 					} else {
 						_bad.add(faces[j]);
 					}
 					break;
 				}
 			}
 		}
 		for(int i=0;i<faces.length;++i) {
 			if(used[i]) {
 				continue;
 			}
 			_FaceEntry newEntry = new _FaceEntry();
 			newEntry.rect = faces[i];
 			newEntry.earliestTimestamp = timestampMs;
 			newEntry.latestTimestamp = timestampMs;
 			newEntries.add(newEntry);
 			_bad.add(faces[i]);
 		}
 		_faceList.faces = newEntries;
 	}
 
 	public FaceTracker(DetectionBasedTracker classifier,
 					   double maxGrowthRate,double maxMoveRate,
 					   double faceDecayTime,double minNewFaceTime,
 					   double faceHoldTime) {
 		_classifier = classifier;
 		_maxGrowthRate = maxGrowthRate;
 		_maxMoveRate = maxMoveRate;
 		_faceDecayTime = (long)(faceDecayTime*1000);
 		_minNewFaceTime = (long)(minNewFaceTime*1000);
 		_faceHoldTime = (long)(faceHoldTime*1000);
 	}
 	
 	public void addFaceSet(final Mat img,long timestampMs,final Mat raw) {
 		boolean found = false;
 		for(int i=0;i<oldMats.size();++i) {
 			if(oldMats.get(i) != null && 
 			   timestampMs-oldMats.get(i).timestampMs > _faceHoldTime) {
 				oldMats.get(i).m.release();
 				oldMats.set(i, null);
 			}
 			if(!found && oldMats.get(i) == null) {
 				oldMats.set(i, new _MatEntry(raw.clone(), timestampMs));
 				found = true;
 			}
 		}
 		if(!found) {
 			oldMats.add(new _MatEntry(raw.clone(),timestampMs));
 		}
 		if(_job != null) {
 			if(_job.isDone()) {
 				try {
 					_updateList(_job.get(),_jobTimestampMs);
 				} catch (InterruptedException e) {}
 				  catch (ExecutionException e)   {}
 			}
 			if(_job.isDone() || _job.isCancelled()) {
 				_job = null;
 			}
 		}
 		if(_job == null) {
 			_jobTimestampMs = timestampMs;
 			_job = _executor.submit(new Callable<Rect[]>() {
 				final Mat _img = img.clone();
 				@Override
 				public Rect[] call() {
 			        MatOfRect faces = new MatOfRect();
 					_classifier.detect(_img,faces);
 					return faces.toArray();
 				}
 			});
 		}
 	}
 	public Rect[] goodFaces() {
 		Rect[] ret = new Rect[_good.size()];
 		return _good.isEmpty() ? ret : _good.toArray(ret);
 	}
 	public Rect[] badFaces() {
 		Rect[] ret = new Rect[_bad.size()];
 		return _bad.isEmpty() ? ret : _bad.toArray(ret);
 	}
 	
 	public void eliminateBad(Mat in) {
 		long timestamp = _faceList.latestTimestamp;
 		for(_FaceEntry entry:_faceList.faces) {
 			if(timestamp-entry.earliestTimestamp < _minNewFaceTime) {
 				int oldest = -1;
 				for(int i=0;i<oldMats.size();++i) {
					if(oldest < 0 || (oldMats != null && oldMats.get(i).timestampMs < oldMats.get(oldest).timestampMs)) {
 						oldest = i;
 					}
 				}
 				if(oldest >= 0) {
 					oldMats.get(oldest).m.submat(entry.rect).copyTo(in.submat(entry.rect));
 				}
 				/*
 				ArrayList<Mat> olderMats = new ArrayList<Mat>();
 				for(_MatEntry mEntry:oldMats) {
 					if(mEntry.timestampMs < entry.earliestTimestamp) {
 						olderMats.add(mEntry.m.submat(entry.rect));
 					}
 				}
 				Mat[] olderMatsArr = new Mat[olderMats.size()];
 				MatAverager.average(in.submat(entry.rect), olderMats.toArray(olderMatsArr));
 				*/
 			}
 		}
 	}
 }
