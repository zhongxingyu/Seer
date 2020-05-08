 package com.example.newecosns.utnils;
 
 import jp.innovationplus.ipp.client.IPPApplicationResourceClient;
 import jp.innovationplus.ipp.core.IPPQueryCallback;
 import jp.innovationplus.ipp.jsontype.IPPApplicationResource;
 import android.content.Context;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.example.newecosns.models.CommentItem;
 import com.example.newecosns.models.StarItem;
 import com.example.newecosns.models.SummaryItem;
 
 public class StarCallback implements OnClickListener {
 
 	private IPPApplicationResource item;
 	private String ipp_auth_key;
 	private Context context;
 	private TextView number_of_star;
 	private Button buton_evaluate_it;
 
 	public StarCallback(IPPApplicationResource item, String ipp_auth_key, Context context, TextView number_of_star) {
 		this.item = item;
 		this.ipp_auth_key = ipp_auth_key;
 		this.context = context;
 		this.number_of_star = number_of_star;
 		;
 	}
 
 	@Override
 	public void onClick(View v) {
 		this.buton_evaluate_it = (Button) v;
 
 		IPPApplicationResourceClient client = new IPPApplicationResourceClient(context);
 		client.setAuthKey(ipp_auth_key);
 
 		StarItem starItem = new StarItem();
 		starItem.setTarget_class(item.getClass().toString());
 		starItem.setTarget_resource_id(item.getResource_id());
 
 
 		//スターのログ保存
 		client.create(StarItem.class, starItem, new IPPQueryCallback<String>() {
 
 			@Override
 			public void ippDidError(int arg0) {
				// TODO 自動生成されたメソッド・スタブ
 
 			}
 
 			@Override
 			public void ippDidFinishLoading(String arg0) {
 				//ビュー上で星の数を増やす
 
 				//一回増やしたら読み込み直さないと二度は押せない
				number_of_star.setText(String.valueOf(Integer.parseInt(number_of_star.getText().toString())+1));
 				buton_evaluate_it.setEnabled(false);
 
 			}
 		});
 
 
 
 
 		//コメント、またはログに対してスターを追加
 		if(item.getClass().toString().equals(SummaryItem.class.toString())){
 			client.get(SummaryItem.class, item.getResource_id(), new GetForUpdateStarCallbackS(context));
 		}else if(item.getClass().toString().equals(CommentItem.class.toString())){
 
 			client.get(CommentItem.class, item.getResource_id(), new GetForUpdateStarCallback(context));
 		}
 
 
 
 	}
 
 	//取得
 	class GetForUpdateStarCallback implements IPPQueryCallback<CommentItem>{
 
 		private Context context;
 
 		public GetForUpdateStarCallback(Context context){
 			this.context = context;
 		}
 
 		@Override
 		public void ippDidError(int arg0) {
 			// TODO 自動生成されたメソッド・スタブ
 
 		}
 
 
 		@Override
 		public void ippDidFinishLoading(CommentItem item) {
 			IPPApplicationResourceClient client = new IPPApplicationResourceClient(context);
 			client.setAuthKey(ipp_auth_key);
 
 			client.delete(CommentItem.class, item.getResource_id(), new GetForUpdateStarCallback2(context, item));
 
 		}
 
 	}
 
 	class GetForUpdateStarCallback2 implements IPPQueryCallback<String>{
 
 		private Context context;
 		private CommentItem item;
 
 		public GetForUpdateStarCallback2(Context context, CommentItem item){
 			this.context = context;
 			this.item = item;
 		}
 
 		@Override
 		public void ippDidError(int arg0) {
 			// TODO 自動生成されたメソッド・スタブ
 
 		}
 
 
 		@Override
 		public void ippDidFinishLoading(String string) {
 			IPPApplicationResourceClient client = new IPPApplicationResourceClient(context);
 			client.setAuthKey(ipp_auth_key);
 			item.setStar(item.getStar() + 1);
 			client.create(CommentItem.class, item, new IPPQueryCallback<String>() {
 
 				@Override
 				public void ippDidError(int arg0) {
 					// TODO 自動生成されたメソッド・スタブ
 
 				}
 
 				@Override
 				public void ippDidFinishLoading(String arg0) {
 					// TODO 自動生成されたメソッド・スタブ
 
 				}
 			});
 		}
 
 	}
 
 	//取得
 	class GetForUpdateStarCallbackS implements IPPQueryCallback<SummaryItem>{
 
 		private Context context;
 
 
 		public GetForUpdateStarCallbackS(Context context){
 			this.context = context;
 		}
 
 		@Override
 		public void ippDidError(int arg0) {
 			// TODO 自動生成されたメソッド・スタブ
 
 		}
 
 
 		@Override
 		public void ippDidFinishLoading(SummaryItem item) {
 			IPPApplicationResourceClient client = new IPPApplicationResourceClient(context);
 			client.setAuthKey(ipp_auth_key);
 
 
 			client.delete(SummaryItem.class, item.getResource_id(), new GetForUpdateStarCallbackS2(context, item));
 		}
 
 	}
 
 	class GetForUpdateStarCallbackS2 implements IPPQueryCallback<String>{
 
 		private Context context;
 		private SummaryItem item;
 
 		public GetForUpdateStarCallbackS2(Context context, SummaryItem item){
 			this.context = context;
 			this.item = item;
 		}
 
 		@Override
 		public void ippDidError(int arg0) {
 			// TODO 自動生成されたメソッド・スタブ
 
 		}
 
 
 		@Override
 		public void ippDidFinishLoading(String string) {
 			IPPApplicationResourceClient client = new IPPApplicationResourceClient(context);
 			client.setAuthKey(ipp_auth_key);
 			item.setStar(item.getStar() + 1);
 			client.create(SummaryItem.class, item, new IPPQueryCallback<String>() {
 
 				@Override
 				public void ippDidError(int arg0) {
 					// TODO 自動生成されたメソッド・スタブ
 
 				}
 
 				@Override
 				public void ippDidFinishLoading(String arg0) {
 					// TODO 自動生成されたメソッド・スタブ
 
 				}
 			});
 		}
 
 	}
 }
