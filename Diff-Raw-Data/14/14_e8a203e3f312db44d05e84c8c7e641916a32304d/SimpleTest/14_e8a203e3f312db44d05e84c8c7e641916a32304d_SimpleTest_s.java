 package com.anthavio.hatatitla;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 
 import java.io.ByteArrayInputStream;
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.fest.assertions.api.Fail;
 import org.testng.annotations.Test;
 
import com.anthavio.hatatitla.Authentication;
import com.anthavio.hatatitla.GetRequest;
import com.anthavio.hatatitla.HttpClient4Config;
import com.anthavio.hatatitla.HttpClient4Sender;
import com.anthavio.hatatitla.HttpSenderConfig;
import com.anthavio.hatatitla.JavaHttpSender;
import com.anthavio.hatatitla.PostRequest;
import com.anthavio.hatatitla.SenderRequest;
 import com.anthavio.hatatitla.Authentication.Scheme;
 import com.anthavio.hatatitla.HttpSender.Multival;
 import com.anthavio.hatatitla.SenderRequest.Method;
 
 /**
  * 
  * @author martin.vanek
  *
  */
 public class SimpleTest {
 
 	@Test
 	public void requestUrl() {
 		JavaHttpSender sender = new JavaHttpSender("www.somewhere.com/path");
 		assertThat(sender.getConfig().getHostUrl().toString()).isEqualTo("http://www.somewhere.com"); //add http prefix and remove path suffix
 		//SimpleHttpSender sender = null;
 
 		GetRequest rGet = new GetRequest("/path");
 		String[] paq = sender.getPathAndQuery(rGet);
 		assertThat(paq[0]).isEqualTo("/path");
 		assertThat(paq[1]).isNull();
 
 		rGet.addParam("p1", (Serializable) null);
 		paq = sender.getPathAndQuery(rGet);
 		assertThat(paq[0]).isEqualTo("/path?p1=");
 		assertThat(paq[1]).isEqualTo("p1=");
 
 		rGet.addParam("p2", 1);
 		paq = sender.getPathAndQuery(rGet);
 		assertThat(paq[0]).isEqualTo("/path?p1=&p2=1");
 		assertThat(paq[1]).isEqualTo("p1=&p2=1");
 
 		rGet.addParam(";m1", null); //add matrix parameter
 		paq = sender.getPathAndQuery(rGet);
 		assertThat(paq[0]).isEqualTo("/path;m1=?p1=&p2=1");
 		assertThat(paq[1]).isEqualTo("p1=&p2=1");
 
 		rGet.addParam(";m2", 2); //add matrix parameter
 		paq = sender.getPathAndQuery(rGet);
 		assertThat(paq[0]).isEqualTo("/path;m1=;m2=2?p1=&p2=1");
 		assertThat(paq[1]).isEqualTo("p1=&p2=1");
 
 		//now with POST request
 
 		PostRequest rPost = new PostRequest("/path");
 		paq = sender.getPathAndQuery(rPost);
 		assertThat(paq[0]).isEqualTo("/path");
 		assertThat(paq[1]).isNull();
 
 		rPost.addParam("p1", null);
 		paq = sender.getPathAndQuery(rPost);
 		assertThat(paq[0]).isEqualTo("/path");
 		assertThat(paq[1]).isEqualTo("p1=");
 
 		rPost.addParam("p2", 1);
 		paq = sender.getPathAndQuery(rPost);
 		assertThat(paq[0]).isEqualTo("/path");
 		assertThat(paq[1]).isEqualTo("p1=&p2=1");
 
 		rPost.addParam(";m1", null); //add matrix parameter
 		paq = sender.getPathAndQuery(rPost);
 		assertThat(paq[0]).isEqualTo("/path;m1=");
 		assertThat(paq[1]).isEqualTo("p1=&p2=1");
 
 		rPost.addParam(";m2", 2); //add matrix parameter
 		paq = sender.getPathAndQuery(rPost);
 		assertThat(paq[0]).isEqualTo("/path;m1=;m2=2");
 		assertThat(paq[1]).isEqualTo("p1=&p2=1");
 
 		//setting custom POST body moves query parameters from body to path 
 		rPost.setBody(new ByteArrayInputStream(new byte[0]), "application/json; charset=utf-8");
 		paq = sender.getPathAndQuery(rPost);
 		assertThat(paq[0]).isEqualTo("/path;m1=;m2=2?p1=&p2=1");
 		assertThat(paq[1]).isEqualTo("p1=&p2=1");
 	}
 
 	@Test
 	public void requestParams() {
 		SenderRequest request = new GetRequest("/");
 		assertThat(request.getMethod()).isEqualTo(Method.GET);
 		assertThat(request.getUrlPath()).isEqualTo("/");
 		assertThat(request.getParameters()).isEmpty();
 		assertThat(request.getHeaders()).isEmpty();
 
 		request = new PostRequest("/");
 		assertThat(request.getMethod()).isEqualTo(Method.POST);
 		assertThat(request.getUrlPath()).isEqualTo("/");
 		assertThat(request.getParameters()).isEmpty();
 		assertThat(request.getHeaders()).isEmpty();
 
 		Multival parameters = new Multival(
 				Arrays.asList(new String[][] { { "p1", "y" }, { "p2", "a", "b", "c" }, { "p3" } }));
 		Map<String, List<String>> mHeaders = new HashMap<String, List<String>>();
 		mHeaders.put("h1", Arrays.asList("v1"));
 		mHeaders.put("h2", Arrays.asList("v21", "v22", "v23"));
 		mHeaders.put("h3", Collections.EMPTY_LIST);
 		Multival headers = new Multival(mHeaders);
 		String path = "/path/to/somewhere?pname=pvalue";
 		request = new PostRequest(path).setParameters(parameters).setHeaders(headers);
 		assertThat(request.getMethod()).isEqualTo(Method.POST);
 		assertThat(request.getUrlPath()).isEqualTo(path);
 
 		assertThat(request.getParameters()).isEqualTo(parameters);
 		assertThat(request.getParameters().size()).isEqualTo(3);
 		assertThat(request.getParameters().names().size()).isEqualTo(3);
 
 		assertThat(request.getParameters().get("p1")).hasSize(1);
 		assertThat(request.getParameters().getFirst("p1")).isEqualTo("y");
 		assertThat(request.getParameters().getLast("p1")).isEqualTo("y");
 		assertThat(request.getParameters().get("p2")).hasSize(3);
 		assertThat(request.getParameters().getFirst("p2")).isEqualTo("a");
 		assertThat(request.getParameters().getLast("p2")).isEqualTo("c");
 		assertThat(request.getParameters().get("p3")).hasSize(1);
 		assertThat(request.getParameters().getFirst("p3")).isEqualTo("");
 		assertThat(request.getParameters().getLast("p3")).isEqualTo("");
 
 		assertThat(request.getHeaders()).isEqualTo(headers);
 		assertThat(request.getHeaders().size()).isEqualTo(3);
 		assertThat(request.getHeaders().names().size()).isEqualTo(3);
 
 		assertThat(request.getHeaders().get("h1")).hasSize(1);
 		assertThat(request.getHeaders().getFirst("h1")).isEqualTo("v1");
 		assertThat(request.getHeaders().getLast("h1")).isEqualTo("v1");
 
 		assertThat(request.getHeaders().get("h2")).hasSize(3);
 		assertThat(request.getHeaders().getFirst("h2")).isEqualTo("v21");
 		assertThat(request.getHeaders().getLast("h2")).isEqualTo("v23");
 
 		assertThat(request.getHeaders().get("h3")).hasSize(1);
 		assertThat(request.getHeaders().getFirst("h3")).isEqualTo("");
 		assertThat(request.getHeaders().getLast("h3")).isEqualTo("");
 
 		//parameteres allow duplicit names
 		request.addParam("param", "something");
 		assertThat(request.getParameters().get("param")).hasSize(1);
 		request.addParam("param", "something");
 		assertThat(request.getParameters().get("param")).hasSize(2);
 
 		//header do not allow duplicit names
 		request.setHeader("header", "something");
 		assertThat(request.getHeaders().get("header")).hasSize(1);
 		request.setHeader("header", "something");
 		assertThat(request.getHeaders().get("header")).hasSize(1);
 	}
 
 	@Test
 	public void simpleDefauts() {
 		String url = "http://www.hostname.com:8080/path/to/somewhere";
 		JavaHttpSender sender = new JavaHttpSender(url);
 		assertThat(sender.getConfig().getHostUrl().toString()).isEqualTo("http://www.hostname.com:8080");// file(path) URL part is thrown away
		assertThat(sender.getConfig().getEncoding()).isEqualTo("UTF-8");
 		assertThat(sender.getConfig().getAuthentication()).isNull();
 
 		//default authentication is BASIC and preepmtive
 		Authentication authentication = new Authentication(Authentication.Scheme.BASIC, "user", "pass");
 		assertThat(authentication.getScheme()).isEqualTo(Authentication.Scheme.BASIC);
 		assertThat(authentication.getPreemptive()).isEqualTo(true);
 	}
 
 	@Test
 	public void http4() {
 		String url = "http://www.hostname.com:8080/path/to/somewhere";
 		HttpClient4Config config = new HttpClient4Config(url);
 		Authentication authentication = new Authentication(Scheme.DIGEST, "user", "pass", false);
 		config.setAuthentication(authentication);
 		HttpClient4Sender sender = new HttpClient4Sender(config);
 
 		assertThat(sender.getConfig().getHostUrl().toString()).isEqualTo("http://www.hostname.com:8080"); // file(path) URL part is thrown away
		assertThat(sender.getConfig().getEncoding()).isEqualTo("UTF-8");
 		assertThat(sender.getConfig().getAuthentication().getPreemptive()).isFalse();
 	}
 
 	@Test
 	public void senderBadParameters() {
 		JavaHttpSender sender;
 		try {
 			sender = new JavaHttpSender((String) null);
 			Fail.fail("Previous statemet must throw IllegalArgumentException");
 		} catch (IllegalArgumentException iax) {
 			//ok
 		}
 		try {
 			sender = new JavaHttpSender((HttpSenderConfig) null);
 			Fail.fail("Previous statemet must throw IllegalArgumentException");
 		} catch (IllegalArgumentException iax) {
 			//ok
 		}
 		try {
 			sender = new JavaHttpSender("");
 			Fail.fail("Previous statemet must throw IllegalArgumentException");
 		} catch (IllegalArgumentException iax) {
 			//ok
 		}
 		try {
 			sender = new JavaHttpSender("http:///");
 			Fail.fail("Previous statemet must throw IllegalArgumentException");
 		} catch (IllegalArgumentException iax) {
 			//ok
 		}
 	}
 
 }
