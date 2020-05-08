 /*******************************************************************************
  * Copyright (c) 2010 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.internal.deltacloud.test.fakes;
 
 /**
  * @author André Dietisheim
  */
 public class ServerKeyResponseFakes {
 
 	public static class KeyActionResponse {
 		public static final String url = "http://localhost:3001/api/keys/test1292840175447";
 		public static final String method = "delete";
 		public static final String name = "destroy";
 		public static final String keyActionResponse = getKeyActionXML(url, method, name);
 	}
 
 	public static class KeyResponse {
 		public static final String url = "http://localhost:3001/api/keys/test1292840175447";
 		public static final String method = "delete";
 		public static final String id = "test1292840175447";
 		public static final String name = "destroy";
 		public static final String fingerprint = "60:7c:f6:9e:e0:a1:52:bc:c0:9a:11:80:a7:1b:f6:8b:c6:55:cd:1f";
 		public static final String pem =
 					"-----BEGIN RSA PRIVATE KEY-----"
 							+ "YkFqsstgVJqYc=sxypCDk=qJbHOmhQNYxaQR4vna=ccPbj68MuxQSZ9tiyu+Z8yAog0DI65/j6u"
 							+ "xE6gTMsqqTrDkGmAwhiGLsgORkQyxEthGyDfA40YaBf5/5F=Cvuj2zpnp63JIrUrqoqI1FQYhnA"
 							+ "U34yKaj8+3/0AqsdEmWsWLMbV4HXaRtGZOPbERnJE28EhLlq/v+9wC59hpIZt6s4K0eRBYxCWz/"
 							+ "xvEG=7wZJi7WE0/tsH9YIAHaLRqyxV7H5kRqaYExZhUpBgf/x745KJlPpr1I20BJSrj6Fw4z4P5"
 							+ "DIUPDWit8aQdnBpO2fq9eQLGZKyWmj5xpzFm5DxbV1K=bdmqCnC6XHTLcfV4fqW1egYg2DK5WCj"
 							+ "nsl+mQjn4CNvEdymhna7+Bw0D3JcPcW/EGUrsBGEGLT/suQbEi8x0vQscpBEAizq5GZaKZ6Kec9"
 							+ "7MOHpx7qDqIAPjH9Y3ben7EaR0O3laY/OPrFREw8jP=mptePHF2r07s52QkdqkbU4ePC5BSWOcb"
 							+ "bhOqypbbv9V8YssYLyt6m3VOJFHOoERaDJQ2fMmqTDuFc87lxDrChJk4cw0q9o6Q+YzEnjTqGQo"
 							+ "XcwTtutpL97f1HjO34XlcHn3B1iZ8lsQGJWry9MWaiCdjj02v0mfN+UpbIQNBX452Xllf8YM//0"
 							+ "Kaylt3GZvr2bJsJ=lQIUIxVzREHd7ym/hRNTBx5qK2/=8h57IdyQHZSnjDT05qDRsSPcm5nQmbM"
 							+ "dgivv0/vXogWg9ehbym4DNez38QVkQaoJuKd/ESBIU2p8PIEXWC13HHzIMDbkbM235nFn3Roj59"
 							+ "xt2AJoQnltdfuhA4+5ApnnIYcWzgkd8vWZPhNL2u40Sw1ZPrM+g4n7H48IdwtE3vZ0XfF3Lpdee"
 							+ "IReubErRzxIMNVz=PrLQMAOhukYNJeH63PdxfSsJf7rtGwA1qEF1WcZ1ibvAuFr0G3KQalGCgCh"
 							+ "zkF63HCWcjafUTJ3jE6/U5ZPu8GrhAQQqu=r3NyzLgoTBaNwfe7ybxvBBofjdmD9xPipOhrQjDC"
 							+ "PDeaMDZ6XzwAddh4fd1K3kl29DXNBmPAgfaG8CgdnHVc/gQgAv40RvWDNnYae0/MGE+qrLN0XXF"
 							+ "1g3qHLkmqdtg88nCH=X7kf6FZZ3LE+bLKIF2Y4Xh3X8sqHlImLWSlKvKu6/CuB4GsrfLxu1VLdc"
 							+ "ee3DxUIaqz3LmkERnT7ALcMBjBjRNp=DR=x7zON0f0Nht0gIj1vvDWQmEzRqGxgTwS2PtGL3bOZ"
 							+ "v2hiV3G3+S/9SAD9rfiW9Ws1YLH5mVDcHcKWhHXoM/UqPj3ob3yGzvYgR+X/dIg7tug/k=TTtD8"
 							+ "1wkG4gTjHkfEhCs05/+PZ4rFG15nVpv06e/a3nXtyDQ77qH3irRPsLZDp/CWFdt=Poe4NLX46gE"
 							+ "nU07L+ueqgZUa8Tq6A9oG7QUyjtJh4ZxkShYkIullvUksW0yppaIeB32Xxw2XVEtdu/v=rFHSHh"
 							+ "HwoZ1A/=ku7ICdMg5gD6U+Zg0YlxniHDaSJ8A6kdt2iUaPaZQQcH8T4yh90CKHhbl5NzhxAu3Jz"
							+ "dc=oRQqdzizw9UrN84wEmQ6r9hDHUq2x14PR=xBzwLGzR2dh73GdjxF5OmOrp3m4yCkw"
							+ "-----END RSA PRIVATE KEY-----";
 		public static final String keyResponse = getKeyResponseXML(id, fingerprint, pem, url, method, name);
 	}
 
 	public static class KeysResponse {
 
 		public static final String url1 = "http://localhost:3001/api/keys/test1292840175417";
 		public static final String method1 = "delete";
 		public static final String id1 = "test1292840175447";
 		public static final String name1 = "destroy";
 		public static final String fingerprint1 = "60:7c:f6:9e:e0:a1:52:bc:c0:9a:11:80:a7:1b:f6:8b:c6:55:cd:1f";
 		public static final String pem1 = "-----BEGIN RSA PRIVATE KEY-----"
 				+ "YkFqsstgVJqYc=sxypCDk=qJbHOmhQNYxaQR4vna=ccPbj68MuxQSZ9tiyu+Z8yAog0DI65/j6u";
 
 		public static final String url2 = "http://localhost:3001/api/keys/test1292840175427";
 		public static final String method2 = "delete";
 		public static final String id2 = "test1292840175447";
 		public static final String name2 = "destroy";
 		public static final String fingerprint2 = "60:7c:f6:9e:e0:a1:52:bc:c0:9a:11:80:a7:1b:f6:8b:c6:55:cd:1f";
 		public static final String pem2 =
 				"-----BEGIN RSA PRIVATE KEY-----"
 						+ "YkFqsstgVJqYc=sxypCDk=qJbHOmhQNYxaQR4vna=ccPbj68MuxQSZ9tiyu+Z8yAog0DI65/j6u";
 
 		public static final String keysResponse =
 					"<keys>"
 							+ getKeyResponseXML(id1, fingerprint1, pem1, url1, method1, name1)
 							+ getKeyResponseXML(id2, fingerprint2, pem2, url2, method2, name2)
 							+ "</keys>";
 	}
 
 	private static final String getKeyActionXML(String url, String method, String name) {
 		return "<link "
 				+ "method='" + method + "' "
 				+ "href='" + url + "' "
 				+ "rel='" + name + "' />";
 	}
 
 	private static final String getKeyResponseXML(String id, String fingerprint, String pem, String url, String method,
 			String name) {
 		return "<key href='" + url + "' id='" + id + "' type='key'>"
 				+ "<actions>"
 				+ getKeyActionXML(url, method, name)
 				+ "</actions>"
 				+ "<fingerprint>" + fingerprint + "</fingerprint>"
 				+ "<pem><pem>" + pem + "</pem></pem>"
 				+ "<state></state>"
 				+ "</key>";
 
 	}
 
 }
