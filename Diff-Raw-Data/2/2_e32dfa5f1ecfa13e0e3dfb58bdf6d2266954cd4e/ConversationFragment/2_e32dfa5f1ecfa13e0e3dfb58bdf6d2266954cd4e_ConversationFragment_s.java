 package ru.tulupov.nsuconnect.fragment;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AbsListView;
 import android.widget.ListView;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import ru.tulupov.nsuconnect.R;
 import ru.tulupov.nsuconnect.adapter.ConversationAdapter;
 import ru.tulupov.nsuconnect.database.ContentUriHelper;
 import ru.tulupov.nsuconnect.database.DatabaseConstants;
 import ru.tulupov.nsuconnect.database.HelperFactory;
 import ru.tulupov.nsuconnect.database.loader.MessageLoader;
 import ru.tulupov.nsuconnect.helper.NotificationHelper;
 import ru.tulupov.nsuconnect.model.Chat;
 import ru.tulupov.nsuconnect.model.Message;
 import ru.tulupov.nsuconnect.service.DataService;
 import ru.tulupov.nsuconnect.util.adapter.BeanHolderAdapter;
 
 
 public class ConversationFragment extends LoaderListFragment<Message> {
 
     private static final String TAG = ConversationFragment.class.getSimpleName();
     private static final String ARGS_CHAT_ID = "chat_id";
 
     private ListView list;
     private View footer;
     private boolean listAtTheEnd;
     private boolean firstTime = true;
 
 
     private BroadcastReceiver updateTypingStatusReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             if (chat.getId() != intent.getIntExtra(DataService.EXTRA_ID, 0)) {
                 return;
             }
             boolean isTyping = intent.getBooleanExtra(DatabaseConstants.EXTRA_IS_TYPING, false);
 
             updateTypingStatus(isTyping);
 
         }
     };
     private ConversationAdapter adapter;
 
     private void updateTypingStatus(boolean isTyping) {
         final View container = footer.findViewById(R.id.container);
 
         if (isTyping) {
             if (container.getVisibility() != View.VISIBLE) {
                 Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
                 container.setVisibility(View.VISIBLE);
                 container.startAnimation(animation);
             }
         } else {
             if (container.getVisibility() != View.GONE) {
                 Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
                 animation.setAnimationListener(new Animation.AnimationListener() {
                     @Override
                     public void onAnimationStart(Animation animation) {
 
                     }
 
                     @Override
                     public void onAnimationEnd(Animation animation) {
                         container.setVisibility(View.GONE);
                     }
 
                     @Override
                     public void onAnimationRepeat(Animation animation) {
 
                     }
                 });
                 container.startAnimation(animation);
             }
         }
     }
 
     private Chat chat;
 
     public static ConversationFragment newInstance(final Context context, int chatId) {
         final Bundle args = new Bundle();
         args.putInt(ARGS_CHAT_ID, chatId);
         return (ConversationFragment) Fragment.instantiate(context, ConversationFragment.class.getName(), args);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fgt_conversation, container, false);
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         list = (ListView) view.findViewById(R.id.list);
         footer = View.inflate(getActivity(), R.layout.footer_messages, null);
         list.addFooterView(footer);
         adapter = new ConversationAdapter();
         super.onViewCreated(view, savedInstanceState);
 
 //        try {
 //            chat = HelperFactory.getHelper().getChatDao().getLast();
 //
 //        } catch (SQLException e) {
 //            Log.e(TAG, "cannot create chat entity", e);
 //        }
 
         chat = new Chat();
         chat.setId(getArguments().getInt(ARGS_CHAT_ID));
 
         list.setOnScrollListener(new AbsListView.OnScrollListener() {
             @Override
             public void onScrollStateChanged(AbsListView absListView, int i) {
 
             }
 
             @Override
             public void onScroll(AbsListView lw, final int firstVisibleItem,
                                  final int visibleItemCount, final int totalItemCount) {
                 final int lastItem = firstVisibleItem + visibleItemCount;
                 listAtTheEnd = (lastItem == totalItemCount);
             }
         });
 
     }
 
 
     @Override
     protected void onLoadFinished() {
         super.onLoadFinished();
         if (listAtTheEnd && !firstTime) {
 
             list.setSelection(adapter.getCount() - 1);
         }
 
         if (firstTime) {
             firstTime = false;
             list.postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     list.setSelection(adapter.getCount() - 1);
                 }
             }, 100);
         }
     }
 
     @Override
     protected Loader<List<Message>> onCreateLoader() {
         return new MessageLoader(getActivity(), chat);
     }
 
     @Override
     protected Uri getContentUri() {
         return ContentUriHelper.getConversationUri(chat.getId());
     }
 
     @Override
     protected BeanHolderAdapter<Message, ?> getAdapter() {
         return adapter;
     }
 
 
     @Override
     public void onResume() {
         super.onResume();
         updateTypingStatus(false);
         IntentFilter updateTypingStatusFilter = new IntentFilter(DatabaseConstants.ACTION_UPDATE_TYPING_STATUS);
         getActivity().registerReceiver(updateTypingStatusReceiver, updateTypingStatusFilter);
 
         updateReadFlag();
 
         NotificationHelper.hideNotification(getActivity());
 
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         getActivity().unregisterReceiver(updateTypingStatusReceiver);
     }
 
 
     @Override
     protected void onDataChange() {
         super.onDataChange();
 
         updateReadFlag();
     }
 
     private void updateReadFlag() {
         try {
             HelperFactory.getHelper().getMessageDao().updateReadFlag(chat);

         } catch (SQLException e) {
             Log.e(TAG, "cannot update read flag", e);
         }
     }
 }
