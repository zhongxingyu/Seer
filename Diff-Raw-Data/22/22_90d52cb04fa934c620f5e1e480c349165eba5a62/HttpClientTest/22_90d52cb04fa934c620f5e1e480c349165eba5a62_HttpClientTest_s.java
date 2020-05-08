 package com.luzi82.libmbgwalpurgis;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
import junit.framework.Assert;

 import org.apache.http.HttpEntity;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.ProtocolException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.DefaultRedirectStrategy;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Node;
 import org.jsoup.nodes.TextNode;
 import org.jsoup.select.Elements;
 import org.junit.Test;
 
 import com.luzi82.libmbgwalpurgis.common.Prop;
 
 public class HttpClientTest {
 
 	final String[] ALL_RANK = { "N", "N+", "R", "R+", "SR", "SR+" };
 	final String[] UPGRADE_OK = { "R+", "SR", "SR+" };
 	final String[] EAT_OK = { "N+", "N" };
 	final List<String> UPGRADE_OK_LIST = Arrays.asList(UPGRADE_OK);
 	final List<String> EAT_OK_LIST = Arrays.asList(EAT_OK);
 
 	@SuppressWarnings("unused")
 	@Test
 	public void testAccess() {
 		try {
 			Properties props=Prop.getAuthProperties();
 
 			HttpParams httpParams = new BasicHttpParams();
 			httpParams.setParameter(CoreProtocolPNames.USER_AGENT, "Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
 			HttpClientParams.setRedirecting(httpParams, true);
 			DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
 			httpClient.setRedirectStrategy(new DefaultRedirectStrategy() {
 				@Override
 				public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
 					if (super.isRedirected(request, response, context))
 						return true;
 					int code = response.getStatusLine().getStatusCode();
 					if (code == 302)
 						return true;
 					return false;
 				}
 			});
 
 			// login
 
 			HttpGet httpGet = new HttpGet("https://ssl.sp.mbga.jp/_lg");
 			HttpResponse response = httpClient.execute(httpGet);
 			System.out.println(response.getStatusLine());
 			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
 
 			HttpEntity httpEntity = response.getEntity();
 			String contentType = httpEntity.getContentType().getValue();
 			System.out.println(contentType);
 			Assert.assertEquals("text/html; charset=UTF-8", contentType);
 
 			InputStream is = httpEntity.getContent();
 			Document doc = Jsoup.parse(is, "UTF-8", "https://ssl.sp.mbga.jp/_lg");
 			EntityUtils.consume(httpEntity);
 
 			Elements formElements = doc.select("form[action=https://ssl.sp.mbga.jp/_lg]");
 			Assert.assertEquals(1, formElements.size());
 			Element formElement = formElements.get(0);
 
 			Elements inputElements = formElement.select("input");
 			for (Element inputElement : inputElements) {
 				System.out.println(inputElement.attr("name") + ": " + inputElement.attr("value"));
 			}
 			Assert.assertEquals(1, inputElements.select("[name=login_id]").size());
 			Assert.assertEquals(1, inputElements.select("[name=login_pw]").size());
 			Assert.assertEquals(1, inputElements.select("[type=submit]").size());
 			for (Element inputElement : inputElements) {
 				String type = inputElement.attr("type");
 				if (type.equals("hidden"))
 					continue;
 				if ((type.equals("email")) && (inputElement.id().equals("login_id")))
 					continue;
 				if ((type.equals("password")) && (inputElement.id().equals("login_pw")))
 					continue;
 				if (type.equals("submit"))
 					continue;
 				Assert.fail("type=" + type);
 			}
 
 			HttpPost httpPost = new HttpPost("https://ssl.sp.mbga.jp/_lg");
 			List<NameValuePair> nvpList = new LinkedList<NameValuePair>();
 			nvpList.add(new BasicNameValuePair("login_id", props.getProperty("login_id")));
 			nvpList.add(new BasicNameValuePair("login_pw", props.getProperty("login_pw")));
 			Elements hiddenInputElements = formElement.select("[type=hidden]");
 			for (Element hiddenInputElement : hiddenInputElements) {
 				nvpList.add(new BasicNameValuePair(hiddenInputElement.attr("name"), hiddenInputElement.attr("value")));
 			}
 			Element submitElement = inputElements.select("[type=submit]").get(0);
 			nvpList.add(new BasicNameValuePair(submitElement.attr("name"), submitElement.attr("value")));
 			StringEntity entity = new StringEntity(URLEncodedUtils.format(nvpList, "UTF-8"));
 			httpPost.setEntity(entity);
 			response = httpClient.execute(httpPost);
 
 			System.out.println(response.getStatusLine());
 			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
 
 			httpEntity = response.getEntity();
 			// is = httpEntity.getContent();
 			// InputStreamReader isr = new InputStreamReader(is, "UTF-8");
 			// char[] buf = new char[1 << 16];
 			// while (true) {
 			// int len = isr.read(buf);
 			// if (len == -1) {
 			// break;
 			// }
 			// System.out.print(Arrays.copyOf(buf, len));
 			// }
 			printEntity(httpEntity);
 			EntityUtils.consume(httpEntity);
 
 			CookieStore cookieStore = httpClient.getCookieStore();
 			List<Cookie> cookieList = cookieStore.getCookies();
 			for (Cookie cookie : cookieList) {
 				System.out.println(cookie.getDomain() + ":" + cookie.getName() + ":" + cookie.getValue());
 			}
 
 			httpGet = new HttpGet("http://sp.pf.mbga.jp/12012090");
 			response = httpClient.execute(httpGet);
 			System.out.println(response.getStatusLine());
 			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
 
 			httpEntity = response.getEntity();
 			// is = httpEntity.getContent();
 			// isr = new InputStreamReader(is, "UTF-8");
 			// while (true) {
 			// int len = isr.read(buf);
 			// if (len == -1) {
 			// break;
 			// }
 			// System.out.print(Arrays.copyOf(buf, len));
 			// }
 			printEntity(httpEntity);
 			EntityUtils.consume(httpEntity);
 
 			// raid_boss_matching_feed.php
 
 			httpGet = new HttpGet("http://sp.pf.mbga.jp/12012090/?url=http%3A%2F%2Fmadoka2.sp.nextory.co.jp%2Fraid_boss_matching_feed.php");
 			response = httpClient.execute(httpGet);
 			System.out.println(response.getStatusLine());
 			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
 
 			httpEntity = response.getEntity();
 			is = httpEntity.getContent();
 			doc = Jsoup.parse(is, "UTF-8", "http://sp.pf.mbga.jp/12012090/?url=http%3A%2F%2Fmadoka2.sp.nextory.co.jp%2Fraid_boss_matching_feed.php");
 			EntityUtils.consume(httpEntity);
 
 			Elements unitElements = doc.select("div[class=basic-bg] td[valign=top]");
 			for (Element unitElement : unitElements) {
 				List<Node> chileNodes = unitElement.childNodes();
 				for (int i = 0; i < chileNodes.size(); ++i) {
 					Node unitElementChild = chileNodes.get(i);
 					if (unitElementChild instanceof Element) {
 						Element unitElementChildElement = (Element) unitElementChild;
 						System.out.println(i + ": <" + unitElementChildElement.tagName() + ">");
 					} else if (unitElementChild instanceof TextNode) {
 						TextNode unitElementChildTextNode = (TextNode) unitElementChild;
 						String txt = unitElementChildTextNode.text();
 						txt = txt.trim();
 						if (!txt.isEmpty()) {
 							System.out.println(i + ": " + txt);
 						}
 					}
 				}
 				Assert.assertEquals(10, chileNodes.size());
 
 				Element e = (Element) chileNodes.get(1);
 				Assert.assertEquals("img", e.tagName());
 				String srcattr = e.attr("src");
 				System.out.println(srcattr);
 				Assert.assertTrue(srcattr.contains("icon01") || srcattr.contains("icon02"));
 
 				TextNode tn = (TextNode) chileNodes.get(2);
 				System.out.println(tn.text().trim());
 
 				e = (Element) chileNodes.get(5);
 				Assert.assertEquals("a", e.tagName());
 				Assert.assertEquals(1, e.children().size());
 				e = e.child(0);
 				Assert.assertEquals("span", e.tagName());
 				System.out.println(e.ownText().trim());
 
 				tn = (TextNode) chileNodes.get(7);
 				System.out.println(tn.text().trim());
 			}
 
 			// mypage.php
 
 			httpGet = new HttpGet("http://sp.pf.mbga.jp/12012090/?url=http%3A%2F%2Fmadoka2.sp.nextory.co.jp%2Fmypage.php");
 			response = httpClient.execute(httpGet);
 			System.out.println(response.getStatusLine());
 			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
 			httpEntity = response.getEntity();
 			is = httpEntity.getContent();
 			doc = Jsoup.parse(is, "UTF-8", "http://sp.pf.mbga.jp/12012090/?url=http%3A%2F%2Fmadoka2.sp.nextory.co.jp%2Fmypage.php");
 			EntityUtils.consume(httpEntity);
 
 			Elements mainStatusElement = doc.select("div[class=main-status] span");
 			for (Element e : mainStatusElement) {
 				String t = e.ownText().trim();
 				if (!t.contains("でレベルアップ"))
 					continue;
 				Elements eev = e.select("span");
 				if (eev.size() != 2)
 					continue;
 				t = eev.get(1).ownText().trim();
 				t = t.replaceAll(Pattern.quote(","), "");
 				System.out.println(Integer.parseInt(t));
 			}
 
 			Elements cardElements = doc.select("div[class=status-exsample] td div");
 			for (Element e : cardElements) {
 				Elements eas = e.select("a");
 				if (eas.size() != 1)
 					continue;
 				Element ea = eas.get(0);
 				if (!ea.attr("href").contains("card_list"))
 					continue;
 				Elements ess = ea.select("span");
 				if (ess.size() != 1)
 					continue;
 				System.out.println(ess.get(0).ownText().trim());
 				System.out.println(e.ownText().trim().replaceAll(Pattern.quote("/"), ""));
 			}
 
 			Pattern lpPattern = Pattern.compile("LP:([\\d]+)/([\\d]+)");
 			Pattern bpPattern = Pattern.compile("BP:([\\d]+)/([\\d]+)");
 			Elements mmlElements = doc.select("span[class=mypage-mode-label]");
 			for (Element e : mmlElements) {
 				String t = e.ownText().trim();
 				Matcher m;
 				m = lpPattern.matcher(t);
 				if (m.find()) {
 					System.out.println(m.group(1));
 					System.out.println(m.group(2));
 				}
 				m = bpPattern.matcher(t);
 				if (m.find()) {
 					System.out.println(m.group(1));
 					System.out.println(m.group(2));
 				}
 			}
 
 			// 強化
 			int girl = 3;
 			String mainGirlCid = null;
 
 			// filter girl, sort by atk up
 			httpGet = new HttpGet("http://sp.pf.mbga.jp/12012090/?guid=ON&url=http%3A%2F%2Fmadoka2.sp.nextory.co.jp%2Fcardadd_change.php%3Fsort%3D7%26kind%3D0%26chara%3D" + girl + "%26equip%3D0");
 			response = httpClient.execute(httpGet);
 			System.out.println(response.getStatusLine());
 			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
 			httpEntity = response.getEntity();
 			is = httpEntity.getContent();
 			doc = Jsoup.parse(is, "UTF-8", "http://sp.pf.mbga.jp/12012090/?guid=ON&url=http%3A%2F%2Fmadoka2.sp.nextory.co.jp%2Fcardadd_change.php%3Fsort%3D7%26kind%3D0%26chara%3D" + girl + "%26equip%3D0");
 			EntityUtils.consume(httpEntity);
 
 			cardElements = doc.select("div[class=basic-bg]");
 			Element cardElement = null;
 			int okRate = -1;
 			for (Element e : cardElements) {
 				Elements rateElements = e.select("a span");
 				for (Element rateElement : rateElements) {
 					String rateText = rateElement.text();
 					int rate = UPGRADE_OK_LIST.indexOf(rateText);
 					if (rate > okRate) {
 						System.out.println(rateText);
 						cardElement = e;
 						okRate = rate;
 					}
 				}
 			}
 
 			if (cardElement != null) {
 				System.out.println("(cardElement != null)");
 				Elements labelElements = cardElement.select("label[for]");
 				Assert.assertEquals(1, labelElements.size());
 				String labelString = labelElements.get(0).attr("for");
 				System.out.println(labelString);
 				Assert.assertTrue(labelString.startsWith("cb"));
 				labelString = labelString.substring(2);
 				mainGirlCid = labelString;
 
 				Elements aElements = cardElement.select("a");
 				String href = null;
 				for (Element e : aElements) {
 					href = e.absUrl("href");
 					if (href.contains("cardadd_change.php"))
 						break;
 				}
 				Assert.assertNotNull(href);
 				System.out.println(href);
 
 				httpGet = new HttpGet(href);
 				response = httpClient.execute(httpGet);
 				System.out.println(response.getStatusLine());
 				Assert.assertEquals(200, response.getStatusLine().getStatusCode());
 				httpEntity = response.getEntity();
 				// is = httpEntity.getContent();
 				// doc = Jsoup.parse(is, "UTF-8", href);
 				EntityUtils.consume(httpEntity);
 
 				// filter girl, sort by atk down
 				String cardadd_list_url = "http://sp.pf.mbga.jp/12012090/?url=http%3A%2F%2Fmadoka2.sp.nextory.co.jp%2Fcardadd_list.php%3Foff%3D%26chara%3D+" + girl + "%26set%3D1%26sort%3D1";
 				httpGet = new HttpGet(cardadd_list_url);
 				response = httpClient.execute(httpGet);
 				System.out.println(response.getStatusLine());
 				Assert.assertEquals(200, response.getStatusLine().getStatusCode());
 				httpEntity = response.getEntity();
 				is = httpEntity.getContent();
 				doc = Jsoup.parse(is, "UTF-8", href);
 				EntityUtils.consume(httpEntity);
 
 				// check first appear card
 				aElements = doc.select("a[href*=card_detail.php][href*=card_sid]");
 				for (Element e : aElements) {
 					href = e.absUrl("href");
 					// must do check
 					System.out.println(href);
 					// must do check
 					Assert.assertTrue(href.contains("card_sid%3D" + labelString));
 					break;
 				}
 
 				LinkedList<String> selectedCid = new LinkedList<String>();
 
 				cardElements = doc.select("form div[class=basic-bg]");
 				for (Element e : cardElements) {
 					Elements checkboxElements = e.select("input[type=checkbox][name^=cards]");
 					if (checkboxElements.size() != 1)
 						continue;
 					String cid = checkboxElements.get(0).attr("value");
 
 					labelElements = e.select("label[for]");
 					Assert.assertEquals(2, labelElements.size());
 					for (Element labelElement : labelElements) {
 						labelString = labelElement.attr("for");
 						Assert.assertTrue(labelString.startsWith("cb"));
 						labelString = labelString.substring(2);
 
 						if (!labelString.equals(cid)) {
 							cid = null;
 							break;
 						}
 					}
 
 					if (cid == null)
 						continue;
 
 					// System.out.println(cid);
 
 					Elements rankElements = e.select("a span");
 					for (Element rateElement : rankElements) {
 						String rankText = rateElement.text();
 						int rate = EAT_OK_LIST.indexOf(rankText);
 						if (rate != -1) {
 							selectedCid.add(cid);
 						}
 					}
 				}
 
 				for (String cid : selectedCid) {
 					System.out.println(cid);
 				}
 				if (selectedCid.size() > 0) {
 					formElements = doc.select("form[action*=cardadd_list.php]:has(input[type=submit])");
 					Assert.assertEquals(1, formElements.size());
 					formElement = formElements.get(0);
 					String formUrl = formElement.absUrl("action");
 					System.out.println(formUrl);
 
 					Elements submitElements = formElement.select("input[type=submit][name=add][value=強化する]");
 					Assert.assertEquals(2, submitElements.size());
 					submitElement = submitElements.get(0);
 
 					httpPost = new HttpPost(formUrl);
 					// httpPost.setHeader("Referer", cardadd_list_url);
 					httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
 					nvpList = new LinkedList<NameValuePair>();
 					nvpList.add(new BasicNameValuePair(submitElement.attr("name"), submitElement.attr("value")));
 					for (String cid : selectedCid) {
 						nvpList.add(new BasicNameValuePair("cards[]", cid));
 					}
 					String entityString = URLEncodedUtils.format(nvpList, "UTF-8");
 					System.out.println(entityString);
 					entity = new StringEntity(entityString);
 					httpPost.setEntity(entity);
 					response = httpClient.execute(httpPost);
 					System.out.println(response.getStatusLine());
 					Assert.assertEquals(200, response.getStatusLine().getStatusCode());
 					httpEntity = response.getEntity();
 					is = httpEntity.getContent();
 					doc = Jsoup.parse(is, "UTF-8", formUrl);
 					EntityUtils.consume(httpEntity);
 
 					// FINAL CHECK
 
 					checkFinalPage(doc, mainGirlCid, girl);
 
 					formElements = doc.select("form[action*=cardadd_exe.php]:has(input[type=submit][name=cardadd])");
 					System.out.println(formElements.size());
 					formElement = formElements.get(0);
 					formUrl = formElement.absUrl("action");
 
 					Elements hiddenElements = formElement.select("input[type=hidden]");
 
 					submitElements = formElement.select("input[type=submit][name=cardadd]");
 					Assert.assertEquals(1, submitElements.size());
 					submitElement = submitElements.get(0);
 
 					System.out.println(formUrl);
 					httpPost = new HttpPost(formUrl);
 					// httpPost.setHeader("Referer", cardadd_list_url);
 					httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
 					nvpList = new LinkedList<NameValuePair>();
 					nvpList.add(new BasicNameValuePair(submitElement.attr("name"), submitElement.attr("value")));
 					for (Element e : hiddenElements) {
 						nvpList.add(new BasicNameValuePair(e.attr("name"), e.attr("value")));
 					}
 					entityString = URLEncodedUtils.format(nvpList, "UTF-8");
 					System.out.println(entityString);
 					entity = new StringEntity(entityString);
 					httpPost.setEntity(entity);
 
 					if (false) {
 						response = httpClient.execute(httpPost);
 						System.out.println(response.getStatusLine());
 						Assert.assertEquals(200, response.getStatusLine().getStatusCode());
 						httpEntity = response.getEntity();
 						printEntity(httpEntity);
 						EntityUtils.consume(httpEntity);
 					}
 				}
 			}
 
 			// present
 			while (true) {
 				System.out.println("present_list");
 				httpGet = new HttpGet("http://sp.pf.mbga.jp/12012090/?url=http%3A%2F%2Fmadoka2.sp.nextory.co.jp%2Fpresent_list.php");
 				try {
 					response = httpClient.execute(httpGet);
 				} catch (ClientProtocolException cpe) {
 					cpe.printStackTrace();
 					Thread.sleep(1000);
 					continue;
 				}
 				break;
 			}
 			System.out.println(response.getStatusLine());
 			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
 			httpEntity = response.getEntity();
 			is = httpEntity.getContent();
 			doc = Jsoup.parse(is, "UTF-8", "http://sp.pf.mbga.jp/12012090/?url=http%3A%2F%2Fmadoka2.sp.nextory.co.jp%2Fpresent_list.php");
 			EntityUtils.consume(httpEntity);
 
 			formElements = doc.select("form[action*=present_exe.php]:has(input[name=sid_ary])");
 			if (formElements.size() >= 1) {
 				Assert.assertEquals(1, formElements.size());
 				formElement = formElements.get(0);
 
 				Elements submitElements = formElement.select("input[type=submit]");
 				Assert.assertEquals(1, submitElements.size());
 				submitElement = submitElements.get(0);
 
 				nvpList = new LinkedList<NameValuePair>();
 				nvpList.add(new BasicNameValuePair(submitElement.attr("name"), submitElement.attr("value")));
 				for (Element e : formElement.select("input[type=hidden]")) {
 					nvpList.add(new BasicNameValuePair(e.attr("name"), e.attr("value")));
 					System.out.println(e.attr("name") + " --- " + e.attr("value"));
 				}
 
 				httpPost = new HttpPost(formElement.absUrl("action"));
 				httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
 				String entityString = URLEncodedUtils.format(nvpList, "UTF-8");
 				System.out.println(entityString);
 				entity = new StringEntity(entityString);
 				httpPost.setEntity(entity);
 
 				response = httpClient.execute(httpPost);
 				System.out.println(response.getStatusLine());
 				Assert.assertEquals(200, response.getStatusLine().getStatusCode());
 				httpEntity = response.getEntity();
 				// printEntity(httpEntity);
 				EntityUtils.consume(httpEntity);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 	}
 
 	public void checkCardCid(Element aElement, String aCid) {
 		Elements aes = aElement.select("a[href*=card_detail.php][href*=card_sid]");
 		Assert.assertEquals(1, aes.size());
 		Assert.assertTrue(aes.attr("href").contains("card_sid%3D" + aCid));
 		Elements labels = aElement.select("label[for]");
 		Assert.assertTrue(labels.size() >= 1);
 		for (Element e : labels) {
 			Assert.assertEquals("cb" + aCid, e.attr("for"));
 		}
 	}
 
 	public void checkCardGirl(Element aElement, int aGirl) {
 		Elements img = aElement.select("img[src*=http%3A%2F%2Fmadoka2.sp.nextory.co.jp%2Fimg%2Fcard%2F]");
 		Assert.assertEquals(1, img.size());
 		Assert.assertTrue(img.attr("src").contains("card_" + aGirl));
 	}
 
 	public void checkCardRank(Element aElement, String[] aRank) {
 		List<String> rankList = Arrays.asList(aRank);
 		Elements aes = aElement.select("a[href*=card_detail.php][href*=card_sid]");
 		Assert.assertEquals(1, aes.size());
 		Elements spans = aes.select("span");
 		for (Element span : spans) {
 			String txt = span.text().trim();
 			if (rankList.contains(txt))
 				return;
 		}
 		for (Element span : spans) {
 			String txt = span.text().trim();
 			System.err.println(txt);
 		}
 		Assert.fail();
 	}
 
 	public void checkFinalPage(Document aDoc, String aMainCid, int aGirl) {
 		Elements cardElements = aDoc.select("div[class=basic-bg]");
 		System.out.println("cardElements.size()=" + cardElements.size());
 		boolean first = true;
 		for (Element cardElement : cardElements) {
 			if (first) {
 				checkCardCid(cardElement, aMainCid);
 				checkCardGirl(cardElement, aGirl);
 				checkCardRank(cardElement, UPGRADE_OK);
 			} else {
 				checkCardGirl(cardElement, aGirl);
 				checkCardRank(cardElement, EAT_OK);
 			}
 			first = false;
 		}
 		Elements blinks = aDoc.select("div[class=blink]");
 		for (Element blink : blinks) {
 			if (blink.text().contains("レアカード"))
 				Assert.fail();
 		}
 	}
 
 	public void printEntity(HttpEntity httpEntity) throws IllegalStateException, IOException {
 		InputStream is = httpEntity.getContent();
 		InputStreamReader isr = new InputStreamReader(is, "UTF-8");
 		char[] buf = new char[1 << 16];
 		while (true) {
 			int len = isr.read(buf);
 			if (len == -1) {
 				break;
 			}
 			System.out.print(Arrays.copyOf(buf, len));
 		}
 	}
 }
