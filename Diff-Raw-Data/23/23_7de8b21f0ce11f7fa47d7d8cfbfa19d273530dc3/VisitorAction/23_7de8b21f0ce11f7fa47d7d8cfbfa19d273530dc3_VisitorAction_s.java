 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.actor.screen.iface.jsf.session;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.Map;
 import java.util.logging.Logger;
 import javax.enterprise.context.Conversation;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import zeroth.actor.screen.app.ApplicationAnnotation.MemberApplicationCDI;
 import zeroth.actor.service.app.actor.MemberApplication;
 import zeroth.actor.service.domain.ActorFilterFactory;
 import zeroth.actor.service.domain.Member;
 import zeroth.actor.service.domain.MemberFilter;
 import zeroth.framework.screen.iface.jsf.FacesHelper;
 /**
  * 訪問者アクション
  * @author nilcy
  */
 @Named("visitorAction")
 @SessionScoped
 // @ServletSecurity(value = @HttpConstraint(rolesAllowed = { SPONSOR, DIRECTOR,
 // CREATOR, EDITOR,
 // AUDITOR, READER }))
 public class VisitorAction implements Serializable {
     /** 製品番号 */
     private static final long serialVersionUID = -6272066593466089850L;
     /** 社員 */
     private Member member;
     /** ログイン日時 */
     private Date loggedInDate;
     /** 社員アプリケーション */
     @Inject
     @MemberApplicationCDI
     private MemberApplication memberApplication;
     /** 会話 */
     @Inject
     private Conversation conversation;
     /** ロガー */
     // @Inject
     // private Logger log;
     private static Logger log = Logger.getGlobal();
     /** コンストラクタ */
     public VisitorAction() {
     }
     /**
      * ログイン有無の確認
      * @return ログイン有無
      */
     @SuppressWarnings("static-method")
     public boolean isLoggedIn() {
        log.info("getUserPrincipal = " + FacesHelper.getExternalContext().getUserPrincipal());
         return FacesHelper.getExternalContext().getUserPrincipal() != null;
     }
     /**
      * {@link #member} の取得
      * @return {@link #member}
      */
     public Member getMember() {
         if ((member == null) && isLoggedIn()) {
             final String account = FacesHelper.getExternalContext().getRemoteUser();
             log.info("account = " + account);
             final MemberFilter filter = ActorFilterFactory.createMemberFilter(account);
             log.info("filter = " + filter);
             member = memberApplication.findOne(filter);
             log.info("member = " + member);
             loggedInDate = new Date();
             // InfraHelper.addSuccessBundleMessage("LoggedIn");
             // InfraHelper.keepMessage();
         } else if (!isLoggedIn()) {
             member = null;
             loggedInDate = null;
         }
         return member;
     }
     /**
      * {@link #loggedInDate} の取得
      * @return {@link #loggedInDate}
      */
     public Date getLoggedInDate() {
         return loggedInDate;
     }
     /**
      * ロール保有の確認ん
      * @param role ロール
      * @return ロール保有
      */
     @SuppressWarnings("static-method")
     public boolean hasRole(final String role) {
         return FacesHelper.getExternalContext().isUserInRole(role);
     }
     /**
      * スタックトレースの取得
      * @return スタックトレース
      */
     @SuppressWarnings("static-method")
     public String getStackTrace() {
         final FacesContext context = FacesContext.getCurrentInstance();
         final Map<String, Object> map = context.getExternalContext().getRequestMap();
         final Throwable throwable = (Throwable) map.get("javax.servlet.error.exception");
         final StringBuilder builder = new StringBuilder();
         final String lf = "<br/>";
         builder.append(throwable.getMessage()).append(lf);
         for (final StackTraceElement element : throwable.getStackTrace()) {
             builder.append(element).append(lf);
         }
         return builder.toString();
     }
     /**
      * {@link #conversation} の取得
      * @return {@link #conversation}
      */
     public Conversation getConversation() {
         return conversation;
     }
 }
