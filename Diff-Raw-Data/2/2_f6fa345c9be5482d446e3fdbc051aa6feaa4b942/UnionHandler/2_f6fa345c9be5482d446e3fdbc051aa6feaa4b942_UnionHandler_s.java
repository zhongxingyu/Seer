 package interdroid.vdb.avro.control.handler;
 
 import interdroid.vdb.avro.model.AvroRecordModel;
 import interdroid.vdb.avro.model.NotBoundException;
 import interdroid.vdb.avro.model.UriBoundAdapter;
 import interdroid.vdb.avro.model.UriDataManager;
 import interdroid.vdb.avro.model.UriUnion;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.avro.Schema;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.net.Uri;
 import android.view.View;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.RadioButton;
 
 public class UnionHandler implements OnCheckedChangeListener {
 	private static final Logger logger = LoggerFactory
 			.getLogger(UnionHandler.class);
 
 	private final AvroRecordModel mDataModel;
 	private final Map<RadioButton, Schema> mSchema = new HashMap<RadioButton, Schema>();
 	private final ValueHandler mValueHandler;
 	private final Map<RadioButton, View> mViews = new HashMap<RadioButton, View>();
 	private final Map<RadioButton, ValueHandler>mHandlers = new HashMap<RadioButton, ValueHandler>();
 	private final UriUnion mUnion;
 
 	public UnionHandler(AvroRecordModel dataModel, ValueHandler valueHandler, UriUnion union) {
 		logger.debug("UnionHanlder built for: {}", union);
 		mDataModel = dataModel;
 		mValueHandler = valueHandler;
 		mUnion = union;
 		mValueHandler.setValue(mUnion);
 	}
 
 	public void addType(RadioButton radioButton, Schema innerType, View view) {
 		mSchema.put(radioButton, innerType);
 		mViews.put(radioButton, view);
 		view.setEnabled(false);
 		radioButton.setOnCheckedChangeListener(this);
 		if (mUnion == null) {
 			if (innerType.getType() == Schema.Type.NULL) {
 				logger.debug("Checking radio since inner union is null and type is NULL");
 				radioButton.setChecked(true);
 			} else {
 				logger.debug("Unchecking radio since inner union is null.");
 				radioButton.setChecked(false);
 			}
 		} else {
 			logger.debug("Checking if type matches: {} {}", innerType.getType(), mUnion.getType());
 			if (isMatchingType(innerType)) {
 				logger.debug("Type match. Enabling.");
 				radioButton.setChecked(true);
 				view.setEnabled(true);
 			}
 		}
 	}
 
 	private boolean isMatchingType(Schema innerType) {
 		logger.debug("Checking for type match: {} {}", innerType.getType(), mUnion.getType());
 		if (innerType.getType().equals(mUnion.getType())) {
 			logger.debug("Checking if type is named.");
 			if (UriBoundAdapter.isNamedType(mUnion.getType())) {
 				logger.debug("Checking if short names match: {} {}", innerType.getName(), mUnion.getTypeName());
 				logger.debug("Checking if long names match: {} {}", innerType.getFullName(), mUnion.getTypeName());
 				if (// Is named and Both names are null
 					((innerType.getName() == null && mUnion.getTypeName() == null) ||
 					// or Is Named and The names match
 					(innerType.getName() != null && innerType.getName().equals(mUnion.getTypeName())) ||
 					(innerType.getFullName() != null && innerType.getFullName().equals(mUnion.getTypeName()))
 					)
 				) {
 					logger.debug("Types match.");
 					return true;
 				}
 			} else {
 				logger.debug("Simple types match.");
 				return true;
 			}
 		}
 		logger.debug("Types don't match");
 		return false;
 	}
 
 	@Override
 	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 		// Uncheck the other radio buttons in the group.
 		if (isChecked) {
 			for (RadioButton button : mSchema.keySet()) {
 				if (button != buttonView) {
 					button.setChecked(false);
 				}
 			}
 			// Set the value based on this button
 			ValueHandler innerHandler = mHandlers.get(buttonView);
 			logger.debug("Union value set to: {} : {}", innerHandler.getValue(), mSchema.get(buttonView));
 			mUnion.setValue(innerHandler.getValue(), mSchema.get(buttonView));
 			mDataModel.onChanged();
 			mViews.get(buttonView).setEnabled(isChecked);
 		}
 	}
 
 	public ValueHandler getHandler(final RadioButton radioButton, final Schema innerSchema) {
 		ValueHandler handler = new ValueHandler() {
 
 			Object mValue = isMatchingType(innerSchema) ? mUnion.getValue() : null;
 
 			@Override
 			public Object getValue() {
 				return mValue;
 			}
 
 			@Override
 			public void setValue(Object value) {
 				mValue = value;
 				onCheckedChanged(radioButton, true);
 			}
 
 			@Override
 			public Uri getValueUri() throws NotBoundException {
 				return mValueHandler.getValueUri();
 			}
 
 			@Override
 			public String getFieldName() {
 				return mValueHandler.getFieldName();
 			}
 		};
 		mHandlers.put(radioButton, handler);
 
 		return handler;
 	}
 
 }
