 package com.slobodastudio.discussions.photon;
 
 import com.slobodastudio.discussions.data.model.ArgPointChanged;
 import com.slobodastudio.discussions.data.model.Point;
 import com.slobodastudio.discussions.data.model.SelectedPoint;
 import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
 import com.slobodastudio.discussions.photon.PhotonController.SyncResultReceiver;
 
 import android.os.Bundle;
 import android.os.ResultReceiver;
 
 public class PhotonHelper {
 
 	/** A private Constructor prevents class from instantiating. */
 	private PhotonHelper() throws UnsupportedOperationException {
 
 		throw new UnsupportedOperationException("Class is prevented from instantiation");
 	}
 
 	public static void sendArgPointChanged(final int statsEvent, final SelectedPoint selectedPoint,
 			final ResultReceiver photonReceiver) {
 
 		if (photonReceiver != null) {
 			final Bundle bundle = new Bundle();
 			bundle.putInt(SyncResultReceiver.EXTRA_STATS_EVENT, statsEvent);
 			bundle.putParcelable(SyncResultReceiver.EXTRA_SELECTED_POINT, selectedPoint);
 			photonReceiver.send(SyncResultReceiver.STATUS_EVENT_CHANGED, bundle);
 		}
 	}
	
	public static void sendArgPointCreated(final Point point, final ResultReceiver photonReceiver) {

		ArgPointChanged argPointChanged = new ArgPointChanged();
		argPointChanged.setEventType(Points.PointChangedType.CREATED);
		argPointChanged.setPointId(point.getId());
		argPointChanged.setTopicId(point.getTopicId());
		PhotonHelper.sendArgPointChanged(argPointChanged, photonReceiver);
	}
 
 	public static void sendArgPointDeleted(final Point point, final ResultReceiver photonReceiver) {
 
 		ArgPointChanged argPointChanged = new ArgPointChanged();
 		argPointChanged.setEventType(Points.PointChangedType.DELETED);
 		argPointChanged.setPointId(point.getId());
 		argPointChanged.setTopicId(point.getTopicId());
 		PhotonHelper.sendArgPointChanged(argPointChanged, photonReceiver);
 	}
 
 	public static void sendArgPointUpdated(final Point point, final ResultReceiver photonReceiver) {
 
 		ArgPointChanged argPointChanged = new ArgPointChanged();
 		argPointChanged.setEventType(Points.PointChangedType.MODIFIED);
 		argPointChanged.setPointId(point.getId());
 		argPointChanged.setTopicId(point.getTopicId());
 		PhotonHelper.sendArgPointChanged(argPointChanged, photonReceiver);
 	}
 
 	public static void sendArgPointUpdated(final SelectedPoint selectedPoint,
 			final ResultReceiver photonReceiver) {
 
 		ArgPointChanged argPointChanged = new ArgPointChanged();
 		argPointChanged.setEventType(Points.PointChangedType.MODIFIED);
 		argPointChanged.setPointId(selectedPoint.getPointId());
 		argPointChanged.setTopicId(selectedPoint.getTopicId());
 		PhotonHelper.sendArgPointChanged(argPointChanged, photonReceiver);
 	}
 
 	public static void sendStatsEvent(final int statsEvent, final SelectedPoint selectedPoint,
 			final ResultReceiver photonReceiver) {
 
 		if (photonReceiver != null) {
 			final Bundle bundle = new Bundle();
 			bundle.putParcelable(SyncResultReceiver.EXTRA_SELECTED_POINT, selectedPoint);
 			bundle.putInt(SyncResultReceiver.EXTRA_STATS_EVENT, statsEvent);
 			photonReceiver.send(SyncResultReceiver.STATUS_EVENT_CHANGED, bundle);
 		}
 	}
 
 	private static void sendArgPointChanged(final ArgPointChanged argPointChanged,
 			final ResultReceiver photonReceiver) {
 
 		if (photonReceiver != null) {
 			final Bundle bundle = new Bundle();
 			bundle.putParcelable(SyncResultReceiver.EXTRA_ARG_POINT_CHANGED, argPointChanged);
 			photonReceiver.send(SyncResultReceiver.STATUS_ARG_POINT_CHANGED, bundle);
 		}
 	}
 }
