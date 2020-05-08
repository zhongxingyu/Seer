 package interdroid.vdb.avro.view.factory;
 
import interdroid.util.view.ViewUtil;
 import interdroid.vdb.avro.control.handler.DateHandler;
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
 import android.widget.DatePicker;
 
 /**
  * Builder for Type.LONG && widget == "date".
  * @author nick &lt;palmer@cs.vu.nl&gt;
  *
  */
 class AvroDateBuilder extends AvroViewBuilder {
 	/**
 	 * Access to logger.
 	 */
 	private static final Logger LOG =
 			LoggerFactory.getLogger(AvroDateBuilder.class);
 
 	/**
 	 * Construct a builder for Type.LONG && widget == "date".
 	 */
 	protected AvroDateBuilder() {
 		super(Type.LONG, "date");
 	}
 
 	/**
 	 * A holder for a DatePicker.
 	 * @author nick &lt;palmer@cs.vu.nl&gt;
 	 *
 	 */
 	private static final class DateViewHolder {
 		/**
 		 * The TimePicker we hold.
 		 */
 		private DatePicker view = null;
 		/**
 		 * @return the time picker we are holding.
 		 */
 		public DatePicker getView() {
 			return view;
 		}
 	};
 
 	@Override
 	public final View buildEditView(final Activity activity,
 			final AvroRecordModel dataModel,
 			final ViewGroup viewGroup, final Schema schema,
 			final String field, final Uri uri,
 			final ValueHandler valueHandler) throws NotBoundException {
 
 		// Unfortunately DatePicker needs a Handler so it has to be initialized
 		// on the UI thread.
 		final DateViewHolder viewHolder = new DateViewHolder();
		
 		activity.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				synchronized (viewHolder) {
 					viewHolder.view = new DatePicker(activity);
 
 					// Build the handler
 					new DateHandler(viewHolder.view, valueHandler);
 
 					// Add it to the view group
 					viewGroup.addView(viewHolder.view);
 
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
 					LOG.error("Interrupted waiting on view.", e);
 				}
 			}
 		}
 
 
 		return viewHolder.view;
 	}
 
 }
