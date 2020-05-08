 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.enterprise.shared;
 import java.util.logging.Logger;
 import javax.enterprise.inject.Default;
 import javax.enterprise.inject.Produces;
 import javax.enterprise.inject.spi.InjectionPoint;
 /**
 * ロガーのファクトリ
  * @author nilcy
  */
 @SuppressWarnings("static-method")
 public class LoggerProducer {
     /** Constructor. */
     public LoggerProducer() {
         super();
     }
     /**
      * ロガーの作成
      * @param aInjectionPoint 注入ポイント
      * @return ロガー
      */
     @Produces
     @Default
     Logger createLogger(final InjectionPoint aInjectionPoint) {
         return Logger.getLogger(aInjectionPoint.getMember().getDeclaringClass().getName());
     }
 }
