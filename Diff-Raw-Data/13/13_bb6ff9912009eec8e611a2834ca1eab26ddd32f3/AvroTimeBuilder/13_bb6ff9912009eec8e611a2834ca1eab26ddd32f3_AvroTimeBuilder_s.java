 package interdroid.vdb.avro.view.factory;
 
import interdroid.util.view.ViewUtil;
 import interdroid.vdb.avro.control.handler.TimeHandler;
 import interdroid.vdb.avro.control.handler.ValueHandler;
 import interdroid.vdb.avro.model.AvroRecordModel;
 import interdroid.vdb.avro.model.NotBoundException;
 
 import org.apache.avro.Schema;
 import org.apache.avro.Schema.Type;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.app.Activity;
 import android.net.Uri;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TimePicker;
 
 /**
  * A builder for Type.LONG && widget == "time".
  * @author nick &lt;palmer@cs.vu.nl&gt;
  *
  */
 class AvroTimeBuilder extends AvroViewBuilder {
 	/**
 	 * Access to logging.
 	 */
 	private static final Logger LOG =
 			LoggerFactory.getLogger(AvroTimeBuilder.class);
 
 	/**
 	 * Build a time builder.
 	 */
 	protected AvroTimeBuilder() {
 		super(Type.LONG, "time");
 	}
 
 	/**
 	 * A holder for a TimePicker.
 	 * @author nick &lt;palmer@cs.vu.nl&gt;
 	 *
 	 */
 	private static final class TimeViewHolder {
 		/**
 		 * The TimePicker we hold.
 		 */
 		private TimePicker view = null;
 		/**
 		 * @return the time picker we are holding.
 		 */
 		public TimePicker getView() {
 			return view;
 		}
 	};
 
 	@Override
 	public final View buildEditView(final Activity activity,
 			final AvroRecordModel dataModel,
 			final ViewGroup viewGroup, final Schema schema,
 			final String field, final Uri uri,
 			final ValueHandler valueHandler) throws NotBoundException {
 
 		// Unfortunately TimePicker needs a Handler so it has to be initialized
 		// on the UI thread.
 		final TimeViewHolder viewHolder = new TimeViewHolder();
 
 		activity.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				synchronized (viewHolder) {
 					viewHolder.view = new TimePicker(activity);
 
 					// Add it to the view group
 					viewGroup.addView(viewHolder.view);
 
					// Build the timeHandler to take data out of view and put it in record
 					new TimeHandler(viewHolder.view, valueHandler);
 
 					viewHolder.notifyAll();
 				}
 			}
 		});
 
 		// Wait for it to finish on the UI thread.
 		synchronized (viewHolder) {
 			while (viewHolder.getView() == null) {
 				try {
 					viewHolder.wait();
 				} catch (InterruptedException e) {
 					LOG.error("Interrupted waiting for view holder.");
 				}
 			}
 		}
 
 		return viewHolder.view;
 	}
 }
