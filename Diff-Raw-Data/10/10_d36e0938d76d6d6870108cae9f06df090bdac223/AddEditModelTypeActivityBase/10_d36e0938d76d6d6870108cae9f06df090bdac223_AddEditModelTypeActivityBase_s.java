 package be.rottenrei.android.lib.app;
 
 import android.app.Activity;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.view.View;
 import be.rottenrei.android.lib.R;
 import be.rottenrei.android.lib.db.DatabaseException;
 import be.rottenrei.android.lib.db.IModelType;
 import be.rottenrei.android.lib.util.ExceptionUtils;
 import be.rottenrei.android.lib.util.UIUtils;
 
 /**
  * Base class for activities that edit and/or add {@link IModelType}s.
  */
 public abstract class AddEditModelTypeActivityBase<ModelType extends IModelType> extends Activity {
 
 	public static final String DELETED_EXTRA = AddEditModelTypeActivityBase.class.getName() + ".deleted";
 
 	private Long dbId;
 
 	protected abstract ModelType getModel();
 
 	protected abstract void setModel(ModelType model);
 
 	protected abstract boolean validate(ModelType model);
 
 	protected abstract void persist(ModelType model) throws DatabaseException;
 
 	protected abstract String getExtra();
 
 	protected abstract Parcelable parcel(ModelType model);
 
 	protected abstract ModelType unparcel(Parcelable parcelable);
 
 	protected abstract int getLayoutId();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(getLayoutId());
 		Intent intent = getIntent();
 		ModelType model = null;
 		if (savedInstanceState != null) {
			model = unparcel(savedInstanceState.getParcelable(getExtra()));
 		} else if (intent != null) {
			model = unparcel(intent.getParcelableExtra(getExtra()));
 		}
 		if (model != null) {
 			setModelInternal(model);
 		}
 	}
 
 	private final ModelType getModelInternal() {
 		ModelType model = getModel();
 		model.setDbId(dbId);
 		return model;
 	}
 
 	private final void setModelInternal(ModelType model) {
 		dbId = model.getDbId();
 		setModel(model);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putParcelable(getExtra(), parcel(getModelInternal()));
 	}
 
 	public void onCancelClicked(@SuppressWarnings("unused") View view) {
 		setResult(RESULT_CANCELED);
 		finish();
 	}
 
 	public void onSaveClicked(@SuppressWarnings("unused") View view) {
 		ModelType model = getModelInternal();
 		if (!validate(model)) {
 			return;
 		}
 		try {
 			persist(model);
 		} catch (DatabaseException e) {
 			ExceptionUtils.handleExceptionWithMessage(e, this, R.string.no_database, AddEditModelTypeActivityBase.class);
 		}
 		UIUtils.informUser(this, R.string.success_edit);
 		Intent intent = new Intent();
 		intent.putExtra(getExtra(), parcel(model));
 		setResult(RESULT_OK, intent);
 		finish();
 	}
 
 	public void onDeleteClicked(@SuppressWarnings("unused") View view) {
 		UIUtils.confirm(this, R.string.confirm_delete, new OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface arg0, int arg1) {
 				performDelete();
 			}
 		});
 	}
 
 	private void performDelete() {
 		ModelType model = getModelInternal();
 		try {
 			delete(model);
 		} catch (DatabaseException e) {
 			ExceptionUtils.handleExceptionWithMessage(e, this, R.string.no_database, AddEditModelTypeActivityBase.class);
 		}
 		UIUtils.informUser(this, R.string.success_delete);
 		Intent intent = new Intent();
 		intent.putExtra(DELETED_EXTRA, true);
 		setResult(RESULT_OK, intent);
 		finish();
 	}
 
 	abstract protected void delete(ModelType model) throws DatabaseException;
 }
