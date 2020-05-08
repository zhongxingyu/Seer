 package z.hol.loadingstate;
 
 import android.content.Context;
 import android.database.DataSetObserver;
 import android.util.AttributeSet;
 import android.widget.AbsListView;
 import android.widget.ListAdapter;
 
 public class AdapterViewWithLoadingState <T extends AbsListView> extends BaseLoadingStateLayout<T>{
     
     private ListAdapter mAdapter;
     private EmptyDataObserver mEmptyObserver;
 
     public AdapterViewWithLoadingState(Context context, AttributeSet attrs,
             int defStyle) {
         super(context, attrs, defStyle);
         // TODO Auto-generated constructor stub
     }
 
     public AdapterViewWithLoadingState(Context context, AttributeSet attrs) {
         super(context, attrs);
         // TODO Auto-generated constructor stub
     }
 
     public AdapterViewWithLoadingState(Context context) {
         super(context);
         // TODO Auto-generated constructor stub
     }
     
     public void setAdapter(ListAdapter adapter){
         if (mEmptyObserver == null){
             mEmptyObserver = new EmptyDataObserver();
         }
         if (mAdapter != null){
             mAdapter.unregisterDataSetObserver(mEmptyObserver);
         }
         mAdapter = adapter;
         if (mAdapter != null && isAutoShowEmpty()){
             mAdapter.registerDataSetObserver(mEmptyObserver);
             if (mAdapter.isEmpty()){
                 empty();
             }
         }
        mDataView.setAdapter(adapter);
     }
     
     public boolean isDataEmpty(){
         if (mAdapter != null){
             return mAdapter.isEmpty();
         }
         return true;
     }
     
     private class EmptyDataObserver extends DataSetObserver{
 
         @Override
         public void onChanged() {
             // TODO Auto-generated method stub
             super.onChanged();
             if (mAdapter == null || mAdapter.isEmpty()){
                 empty();
             }else{
                 hideEmpty();
             }
         }
 
         @Override
         public void onInvalidated() {
             // TODO Auto-generated method stub
             super.onInvalidated();
             if (mAdapter == null || mAdapter.isEmpty()){
                 empty();
             }else{
                 hideEmpty();
             }
         }
     }
 }
