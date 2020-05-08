 package org.tsinghua.omedia.serverAPI;
 
 import org.tsinghua.omedia.R;
 import org.tsinghua.omedia.activity.OmediaActivityIntf;
 import org.tsinghua.omedia.consts.ResultCode;
 import org.tsinghua.omedia.consts.UrlConst;
 import org.tsinghua.omedia.data.EmptyInstance;
 import org.tsinghua.omedia.data.EmptyInstance.EmptyResultType;
 import org.tsinghua.omedia.form.RegisterForm;
 
 /**
  * 
  * @author xuhongfeng
  *
  */
 public abstract class RegisterAPI extends AbstractServerAPI<RegisterForm> {
 
     
     protected RegisterAPI(RegisterForm form, OmediaActivityIntf omediaActivity) {
         super(form, omediaActivity);
     }
 
     @Override
     protected String getUrl() {
         return UrlConst.RegisterUrl;
     }
 
     @Override
     protected void initResultCodeListener() {
         registerResultCodeListener(ResultCode.Register.USERNAME_EXIST,
                 new ResultCodeListener<EmptyInstance.EmptyResultType>(EmptyResultType.class) {
 
             @Override
             protected void innerRun(EmptyResultType result) {
                 omediaActivity.showAlertDialog(R.string.username_exist);
             }
         });
        registerResultCodeListener(ResultCode.SUCCESS,
                new ResultCodeListener<EmptyInstance.EmptyResultType>(EmptyResultType.class) {

            @Override
            protected void innerRun(EmptyResultType result) {
                onSuccess();
            }
        });
     }
     
     protected abstract void onSuccess();
 }
