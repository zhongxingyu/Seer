 package jp.juggler.ImgurMush.helper;
 
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.database.DataSetObserver;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 import jp.juggler.ImgurMush.R;
 import jp.juggler.ImgurMush.data.ResizePreset;
 import jp.juggler.util.LifeCycleListener;
 
 public class ResizePresetAdapter extends BaseAdapter{
 	final BaseActivity act;
 	final String strNoResize;
 	final String strNewPreset;
 	final Cursor preset_cursor; 
 	final ResizePreset.ColumnIndex colidx = new ResizePreset.ColumnIndex();
 	boolean mDataValid = true;
 	
 	public ResizePresetAdapter(BaseActivity act,String strNoResize,String strNewPreset){
 		this.act = act;
 		this.strNoResize = strNoResize;
 		this.strNewPreset = strNewPreset;
		this.preset_cursor = act.cr.query(ResizePreset.meta.uri,null,null,null,ResizePreset.COL_ID+" asc");
 		
 
 		preset_cursor.registerContentObserver(new ContentObserver(act.ui_handler) {
 			@Override
 			public boolean deliverSelfNotifications() {
 				return false;
 			}
 
 			@SuppressWarnings("deprecation")
 			@Override
 			public void onChange(boolean selfChange) {
 				mDataValid = preset_cursor.requery();
 			}
 		});
 		preset_cursor.registerDataSetObserver(new DataSetObserver() {
 
 			@Override
 			public void onChanged() {
 				mDataValid = true;
 				notifyDataSetChanged();
 			}
 
 			@Override
 			public void onInvalidated() {
 				mDataValid = false;
 				notifyDataSetInvalidated();
 			}
 			
 		});
 		act.lifecycle_manager.add(activity_listener);
 	}
 
 	LifeCycleListener activity_listener = new LifeCycleListener(){
 		@Override
 		public void onDestroy() {
 			super.onDestroy();
 			preset_cursor.close();
 		}
 	};
 	
 	@Override
 	public int getCount() {
 		if(!mDataValid) return 0;
 		return preset_cursor.getCount()+2;
 	}
 	
 	@Override
 	public Object getItem(int position) {
 		if(!mDataValid) return null;
 		position--;
 		if(position<0 || position >= preset_cursor.getCount() ) return null;
 		return ResizePreset.loadFromCursor(preset_cursor,colidx,position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return 0;
 	}
 	@Override
 	public View getView(int position, View view, ViewGroup parent) {
 		return make_view( position,  view,  parent,R.layout.lv_resize_preset);
 	}
 	
 	static class ViewHolder {
 		TextView tvName;
 	}
 	
 	private View make_view(int position, View view, ViewGroup parent,int layout){
 		ViewHolder holder;
 		if(view==null){
 			view = act.inflater.inflate(layout ,null );
 			view.setTag( holder = new ViewHolder() );
 			holder.tvName = (TextView)view;
 		}else{
 			holder = (ViewHolder)view.getTag();
 		}
 		try{
 			if( position <= 0 ){
 				holder.tvName.setText(strNoResize);
 			}else if(position >= preset_cursor.getCount()+1 ){
 				holder.tvName.setText(strNewPreset);
 			}else{
 				ResizePreset item = (ResizePreset)getItem(position);
 				holder.tvName.setText( item == null ? strNoResize : item.makeTitle(act) );
 			}
 		}catch(Throwable ex){
 			holder.tvName.setText("(error)");
 		}
 		return view;
 	}
 
 
 }
