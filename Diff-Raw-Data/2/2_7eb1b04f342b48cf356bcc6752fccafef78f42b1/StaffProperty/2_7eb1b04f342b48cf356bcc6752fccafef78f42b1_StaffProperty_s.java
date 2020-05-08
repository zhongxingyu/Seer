 package org.jenkinsci.plugins.staff;
 
 import hudson.Extension;
 import hudson.Util;
 import hudson.model.User;
 import hudson.model.UserProperty;
 import hudson.model.UserPropertyDescriptor;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * スタッフの情報を管理します。
  * 
  * 以下の情報を管理します。
  * 
  * - 電話番号
  * 
  * @author Seiji Sogabe
  */
 public class StaffProperty extends UserProperty {
 
     /**
      * 電話番号 
      */
     private final String telephone;
 
     /**
      * 電話番号を取得します。
      * 
      * jellyファイルで参照するために必要です。
      * 
      * @return  電話番号
      */
     public String getTelephone() {
         return telephone;
     }
 
     /**
      * コンストラクタです。
      * 
      * 画面からの入力値と引数をマッピングする必要があるので、@DataBoundConstructorを
      * 付与します。
      * jellyファイルのfieldの値と引数名を一致させます。
      * 
      * @param telephone
      */
     @DataBoundConstructor
     public StaffProperty(String telephone) {
         // 前後の空白を削除して、空文字ならnullにします
         this.telephone = Util.fixEmptyAndTrim(telephone);
     }
 
     /**
      * {@lik StaffProperty}クラスのインスタンスを管理するディスクリプタ。
      * 
     * 一部のエンドポイント以外は必要です。
      * @Extensionを付与します。
      */
     @Extension
     public static class UserDepartmentPropertyDescriptor extends UserPropertyDescriptor {
 
         /**
          * 新規に{@lik StaffProperty}クラスのインスタンスを生成します。
          * 
          * ユーザ作成等で新規ユーザを生成したときに呼ばれます。
          * このときは、情報は入力されないので、空のインスタンスを返します。
          * 
          * @param user ユーザ
          * @return {@lik StaffProperty} 
          */
         @Override
         public UserProperty newInstance(User user) {
             return new StaffProperty(null);
         }
 
         /**
          * {@lik StaffProperty}クラスのインスタンスを生成します。
          * 
          * ユーザ画面で入力した情報を取得し、インスタンスを生成します。
          * 通常、オーバーライドしなくても、入力した情報を取得し、{@link  StaffProperty}の
          * @DataBoundConstructorが付与されたコンストラクタが呼ばれます。
          * 
          * @param user ユーザ
          * @return {@lik StaffProperty} 
          */
         @Override
         public UserProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             return super.newInstance(req, formData);
         }
 
         /**
          * 画面に表示する名称です。
          * 
          * 入力画面で管理する情報全体のタイトルとして表示されます。
          */
         @Override
         public String getDisplayName() {
             return "スタッフ情報";
         }
     }
 }
