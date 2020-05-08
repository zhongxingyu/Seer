 package ru.tulupov.nsuconnect.fragment;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.support.v4.content.Loader;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.ListView;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import ru.tulupov.nsuconnect.R;
 import ru.tulupov.nsuconnect.adapter.MessageAdapter;
 import ru.tulupov.nsuconnect.database.DatabaseConstants;
 import ru.tulupov.nsuconnect.database.HelperFactory;
 import ru.tulupov.nsuconnect.database.loader.MessageLoader;
 import ru.tulupov.nsuconnect.model.Message;
 import ru.tulupov.nsuconnect.service.DataService;
 import ru.tulupov.nsuconnect.util.adapter.AdapterLoaderCallback;
 
 
 public class ChatFragment extends Fragment {
     public static ChatFragment newInstance(final Context context) {
         return (ChatFragment) Fragment.instantiate(context, ChatFragment.class.getName());
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fgt_chat, container, false);
     }
 
    private boolean isTyping;

     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
 
         if (savedInstanceState == null) {
 
             getChildFragmentManager().beginTransaction()
                     .add(R.id.message_container, MessagesFragment.newInstance(getActivity()))
                     .commit();
         }
         getActivity().startService(new Intent(getActivity(), DataService.class).setAction(DataService.ACTION_LOGIN));
 
 //        view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
 //            @Override
 //            public void onClick(View view) {
 //                Message message = new Message();
 //                message.setMessage("fff " + System.currentTimeMillis());
 //                try {
 //                    HelperFactory.getHelper().getMessageDao().create(message);
 //
 //                    getActivity().sendBroadcast(new Intent(DatabaseConstants.ACTION_UPDATE_MESSAGE_LIST));
 //                } catch (SQLException e) {
 //                    Log.e("xxx", e.getLocalizedMessage());
 //                }
 //            }
 //        });
 
         final EditText edit = (EditText) view.findViewById(R.id.edit);
         view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 getActivity().startService(new Intent(getActivity(), DataService.class).setAction(DataService.ACTION_SEND_MESSAGE).putExtra(DataService.EXTRA_MESSAGE, edit.getText().toString()));
                 edit.setText(null);
             }
         });
 
         edit.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
 
             }
 
             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (edit.getText().length() == 0) {
                    isTyping = false;
                    handler.removeCallbacks(stopTypingRunnable);
                    getActivity().startService(new Intent(getActivity(), DataService.class).setAction(DataService.ACTION_STOP_TYPING));
                } else {
                    if (!isTyping) {
                        getActivity().startService(new Intent(getActivity(), DataService.class).setAction(DataService.ACTION_START_TYPING));
                        handler.removeCallbacks(stopTypingRunnable);
                        handler.postDelayed(stopTypingRunnable, TYPING_TIMEOUT);
                        isTyping = true;
                    }
                }

             }
 
             @Override
             public void afterTextChanged(Editable editable) {
 
             }
         });
     }
 
     private Runnable stopTypingRunnable = new Runnable() {
         @Override
         public void run() {
            isTyping = false;
            getActivity().startService(new Intent(getActivity(), DataService.class).setAction(DataService.ACTION_STOP_TYPING));
         }
     };
 
    private static final long TYPING_TIMEOUT = 1500;
     private Handler handler = new Handler();
 }
