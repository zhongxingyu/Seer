 package com.farpost.spring;
 
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.testng.annotations.Test;
 
 import javax.servlet.http.HttpServletRequest;
 
 import static com.farpost.spring.UrlBuilder.build;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 public class UrlBuilderTest {
 	private final HttpServletRequest request = new RequestStub("/");
 
 	@Test
 	public void shouldBeAbleToBuildUrlForSimpleMappings() {
 		class Controller {
 			@RequestMapping("/hello")
 			public void handle() {}
 		}
 
 		String url = build(request, Controller.class, "#handle").asString();
 		assertThat(url, equalTo("/hello"));
 	}
 
 	@Test
 	public void shouldBeAbleToBuildUrlWithParameters() {
 		class Controller {
 			@RequestMapping("/hello")
 			public void handle() {}
 		}
 
 		String url = build(request, Controller.class, "#handle").
 			parameter("name", "John").
 			asString();
 		assertThat(url, equalTo("/hello?name=John"));
 	}
 
 	@Test
 	public void shouldBeAbleToPassParametersInPattern() {
 		class Controller {
 			@RequestMapping("/blog/{date}")
 			public void handle() {}
 		}
 
 		String url = build(request, Controller.class, "#handle").
 			parameter("date", "2010-01-21").
 			asString();
 		assertThat(url, equalTo("/blog/2010-01-21"));
 	}
 
 	@Test
 	public void shouldBeAbleToProcessClassLevelAnnotations() {
 
 		@RequestMapping("/module/*")
 		class Controller {
 			@RequestMapping("foo")
 			public void handle() {}
 		}
 
 		String url = build(request, Controller.class, "#handle").asString();
 		assertThat(url, equalTo("/module/foo"));
 	}

	@Test
	public void shouldBeAbleToProcessContextUrls() {

		class Controller {
			@RequestMapping("/foo")
			public void handle() {}
		}

		String url = build(new RequestStub("/bar"), Controller.class, "#handle").asString();
		assertThat(url, equalTo("/bar/foo"));
	}
 }
