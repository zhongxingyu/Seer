 /**
  * Copyright (C) 2013- k-minemoto
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.jp.saf.sastruts.method.annotation;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 import net.jp.saf.sastruts.method.enums.ExtensionHeaderBehavior;
 import net.jp.saf.sastruts.method.enums.HttpMethod;
 
 /**
  * 利用できるHTTPメソッドを制限するためのアノテーションです.
  * 
  * Actionクラスの実行メソッドに設定します.
  * 
  * @author k-minemoto
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.METHOD)
 @Documented
 public @interface HttpMethodLimit {
 
 	/**
 	 * 許可する{@link HttpMethod}の配列.
 	 * 
 	 * @return このアノテーションで個別指定しない場合はデフォルトの空配列を返すこと.
 	 */
 	HttpMethod[] value() default {};
 
 	/**
 	 * チェック後のforwardで呼び出された時もチェックするか.
 	 *
 	 * @return チェックする場合true. デフォルトはfalse(しない)
 	 */
 	boolean checkForward() default false;
 
 	/**
 	 * 拡張ヘッダーの扱いの設定.
 	 *
	 * @return デフォルトは{@link ExtensionHeaderBehavior.NOT_DEFINE}(個別設定しない)
 	 */
 	ExtensionHeaderBehavior extensionHeader() default ExtensionHeaderBehavior.NOT_DEFINE;
 }
