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
 package net.jp.saf.sastruts.method.impl;
 
 import java.util.EnumSet;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.jp.saf.sastruts.method.CheckResult;
 import net.jp.saf.sastruts.method.HttpMethodChecker;
 import net.jp.saf.sastruts.method.annotation.HttpMethodLimit;
 import net.jp.saf.sastruts.method.enums.ExtensionHeaderBehavior;
 import net.jp.saf.sastruts.method.enums.HttpMethod;
 import net.jp.saf.sastruts.method.helpers.ErrorHandler;
 import net.jp.saf.sastruts.method.helpers.impl.MethodNotAllowedErrorHandler;
 import org.seasar.framework.container.annotation.tiger.Binding;
 import org.seasar.framework.container.annotation.tiger.BindingType;
 import org.seasar.framework.util.StringUtil;
 
 /**
  * HTTPリクエストの実行メソッドをチェックします.
  *
  * @author k-minemoto
  *
  */
 public class HttpMethodCheckerImpl implements HttpMethodChecker {
 
 	/** serialVersionUID */
 	private static final long serialVersionUID = 1L;
 
 	/** 拡張ヘッダー名 */
 	protected String extensionHeaderName = DEFAULT_EXTENSION_HEADER_NAME;
 
 	/** 拡張ヘッダーの扱い */
 	protected ExtensionHeaderBehavior defaultExtHeaderBehavior = ExtensionHeaderBehavior.NOT_USE;
 
 	/**
 	 * checkのallowsがnullだった時の許可メソッド.
 	 * <p>
 	 * この状態は、アノテーションが存在しなかった場合に発生します.
 	 * </p>
 	 */
 	protected HttpMethod[] defaultAllows =  {};
 	/**
 	 * checkのallowsが空(length=0)だった時の許可メソッド.
 	 * <p>
 	 * この状態は、アノテーションは存在したがvalueが未設定(空)の場合に発生します.
 	 * </p>
 	 */
 	protected HttpMethod[] defaultAnnotationAllows = {HttpMethod.GET_POST};
 
 	/**
 	 * チェックエラー時のハンドラ
 	 */
 	protected ErrorHandler errorHandler = new MethodNotAllowedErrorHandler();
 
 	/**
 	 * 拡張ヘッダー名を設定します.
 	 *
 	 * @param extensionHeaderName 拡張ヘッダー名. 省略時は{@link HttpMethodChecker#DEFAULT_EXTENSION_HEADER_NAME}
 	 */
 	@Binding(bindingType=BindingType.MAY)
 	public void setExtensionHeaderName(String extensionHeaderName) {
 		this.extensionHeaderName = extensionHeaderName;
 	}
 
 	/**
 	 * 拡張ヘッダーの扱いを設定します.
 	 *
	 * @param extensionHeader 拡張ヘッダーの扱い. 省略時は{@link ExtensionHeaderBehavior.NOT_USE}.
 	 */
 	@Binding(bindingType=BindingType.MAY)
 	public void setExtensionHeader(ExtensionHeaderBehavior extensionHeader) {
 		this.defaultExtHeaderBehavior = extensionHeader;
 	}
 
 	/**
 	 * {@link HttpMethodLimit}が付いて無いメソッドに対しての、デフォルトチェックメソッドを設定します.
 	 *
 	 * @param defaultAllows 許可するHTTPメソッド. 省略時は空のリスト(チェックしない)
 	 */
 	@Binding(bindingType=BindingType.MAY)
 	public void setDefaultAllows(List<HttpMethod> defaultAllows) {
 		if (defaultAllows == null || defaultAllows.isEmpty()) {
 			this.defaultAllows =  new HttpMethod[]{};
 		} else {
 			this.defaultAllows =  EnumSet.copyOf(defaultAllows).toArray(new HttpMethod[0]);
 		}
 	}
 
 	/**
 	 * {@link HttpMethodLimit}の、value省略時のデフォルトチェックメソッドを設定します.
 	 *
	 * @param defaultAllows 許可するHTTPメソッド. 省略時は{@link HttpMethod.GET_POST}(GET,POSTのみ)
 	 */
 	@Binding(bindingType=BindingType.MAY)
 	public void setDefaultAnnotationAllows(List<HttpMethod> defaultAnnotationAllows) {
 		if (defaultAnnotationAllows == null || defaultAnnotationAllows.isEmpty()) {
 			this.defaultAnnotationAllows = new HttpMethod[]{};
 		} else {
 			this.defaultAnnotationAllows = EnumSet.copyOf(defaultAnnotationAllows).toArray(new HttpMethod[0]);
 		}
 	}
 
 	/**
 	 * チェックエラー時のハンドラを設定します.
 	 *
 	 * @param errorHandler ハンドラクラス. 省略時は{@link MethodNotAllowedErrorHandler}
 	 */
 	@Binding(bindingType=BindingType.MAY)
 	public void setErrorHandler(ErrorHandler errorHandler) {
 		this.errorHandler = errorHandler;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public CheckResult check(HttpServletRequest request, HttpMethod[] allows, ExtensionHeaderBehavior extensionHeaderBehavior) {
 		HttpMethod[] checkAllows = null;
 		if (allows != null) {
 			checkAllows = allows;
 			if (allows.length == 0) {
 				checkAllows = defaultAnnotationAllows;
 			}
 		} else {
 			checkAllows = defaultAllows;
 		}
 		final String httpMethod = detectCurrentHttpMethod(request, extensionHeaderBehavior);
 		boolean ok = true;
 		if (checkAllows.length > 0) {
 			ok = false;
 			for(HttpMethod allow : checkAllows) {
 				if (allow.check(httpMethod)) {
 					ok = true;
 					break;
 				}
 			}
 		}
 		return ok ? CheckResult.ok(httpMethod, checkAllows) : CheckResult.ng(httpMethod, checkAllows);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String handleError(HttpServletRequest request,
 			HttpServletResponse response, HttpMethod[] allowed, String method) {
 		return this.errorHandler.error(request, response, allowed, method);
 	}
 
 	/**
 	 * 現在のHTTPリクエストから、呼び出されたHTTPメソッドを抽出します.
 	 *
 	 * @param request HTTPリクエスト
 	 * @param customBehavior 個別指定の拡張ヘッダー扱い
 	 * @return HTTPメソッド. ただしHTTPヘッダーから取り出す場合、nullも含めて不明な値が入る可能性がある
 	 */
 	protected String detectCurrentHttpMethod(HttpServletRequest request, ExtensionHeaderBehavior customBehavior) {
 		ExtensionHeaderBehavior behavior = this.defaultExtHeaderBehavior;
 		if (customBehavior != null && customBehavior != ExtensionHeaderBehavior.NOT_DEFINE) {
 			behavior = customBehavior;
 		}
 		//
 		if (behavior == ExtensionHeaderBehavior.NOT_USE || behavior == ExtensionHeaderBehavior.NOT_DEFINE) {
 			return request.getMethod();
 		}
 		if (behavior == ExtensionHeaderBehavior.HEADER_ONLY) {
 			return request.getHeader(this.extensionHeaderName);
 		}
 		String method = null;
 		if (behavior == ExtensionHeaderBehavior.HEADER_FIRST) {
 			method = request.getHeader(this.extensionHeaderName);
 		}
 		if (StringUtil.isEmpty(method)) {
 			return request.getMethod();
 		}
 		return method;
 	}
 }
