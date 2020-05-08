 import java.lang.reflect.Method;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import models.FuzzyDate;
 import models.LifeStory;
 import models.Location;
 import models.Memento;
 import models.SecurityRole;
 import models.User;
 import models.Participation;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import play.Application;
 import play.GlobalSettings;
 import play.Logger;
 import play.data.format.Formatters;
 import play.mvc.Action;
 import play.mvc.Call;
 import play.mvc.Http.Context;
 import play.mvc.Http.Request;
 import play.mvc.Result;
 import play.mvc.Results;
 import pojos.CityBean;
 import pojos.PersonBean;
 import providers.MyUsernamePasswordAuthUser;
 
 import com.feth.play.module.pa.PlayAuthenticate;
 import com.feth.play.module.pa.PlayAuthenticate.Resolver;
 import com.feth.play.module.pa.exceptions.AccessDeniedException;
 import com.feth.play.module.pa.exceptions.AuthException;
 
 import controllers.routes;
 
 public class Global extends GlobalSettings {
 	/**
 	 * Call to create the root Action of a request for a Java application. The
 	 * request and actionMethod values are passed for information.
 	 * 
 	 * @param request
 	 *            The HTTP Request
 	 * @param actionMethod
 	 *            The action method containing the user code for this Action.
 	 * @return The default implementation returns a raw Action calling the
 	 *         method.
 	 */
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Action onRequest(Request request, Method actionMethod) {
 		return new Action.Simple() {
 			public Result call(Context ctx) throws Throwable {
 				Result r = delegate.call(ctx);
 				ctx.session().clear();
 				// Instead of passing the session key in a cookie, we pass it in
 				// a header
 				// String context =
 				// Configuration.root().getString("application.context");
 				// ctx.response().discardCookie("PLAY_SESSION");
 				// ctx.response().discardCookie("PLAY_SESSION", context);
 				Logger.debug("--> Request Headers: ");
 				
 				String contentType = "Content-Type";
 				String origin = "Origin";
 				String host = "Host";
 				String cookie = "Cookie";
 				String xForwardedHost = "X-Forwarded-Host";
 				String xForwardedServer = "X-Forwarded-Server";
 				String xForwardedFor = "X-Forwarded-For";
 				String connection = "Connection";
 				String contentLenght = "Content-Length";
 				String accept = "Accept";
 				String cacheControl = "Cache-Control";
 				String acceptLanguage = "Accept-Language";
 				String acceptEncoding="Accept-Encoding";
 				String userAgent = "User-Agent"; 
 							
 				Logger.debug(contentType+"="+ctx.request().getHeader(contentType));
 				Logger.debug(origin+"="+ctx.request().getHeader(origin));
 				Logger.debug(host+"="+ctx.request().getHeader(host));
 				Logger.debug(cookie+"="+ctx.request().getHeader(cookie));
 				Logger.debug(xForwardedHost+"="+ctx.request().getHeader(xForwardedHost));
 				Logger.debug(xForwardedFor+"="+ctx.request().getHeader(xForwardedFor));
 				Logger.debug(xForwardedServer+"="+ctx.request().getHeader(xForwardedServer));
 				Logger.debug(connection+"="+ctx.request().getHeader(connection));
 				Logger.debug(contentLenght+"="+ctx.request().getHeader(contentLenght));
 				Logger.debug(accept+"="+ctx.request().getHeader(accept));
 				Logger.debug(acceptLanguage+"="+ctx.request().getHeader(acceptLanguage));
 				Logger.debug(cacheControl+"="+ctx.request().getHeader(cacheControl));
 				Logger.debug(acceptEncoding+"="+ctx.request().getHeader(acceptEncoding));
 				Logger.debug(userAgent+"="+ctx.request().getHeader(userAgent));
 
 				Logger.debug("Body ="+ctx.request().body().toString());
 				
 				Logger.debug("--> Setting CORS response headers");
 				ctx.response().setHeader("Access-Control-Allow-Origin", "*");
 				ctx.response().setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT, OPTIONS");
 				ctx.response()
 						.setHeader("Access-Control-Allow-Headers",
 								"accept, origin, Content-type, x-json, x-prototype-version, x-requested-with, PLAY_SESSION");
 				
 				return r;
 			}
 		};
 	}
 	
 	@Override
 	public Result onBadRequest(play.mvc.Http.RequestHeader arg0, String arg1) {
 		Logger.debug("-----> Bad request error "+ arg1);
 		Logger.debug("-----> Bad request error "+ arg0.toString());
 		return Results.badRequest("Don't try to hack the URI!");
 	};
 
 	static class InitialData {
 
 		@SuppressWarnings("deprecation")
 		public static void insert(Application app) {
 
 			
 			
 			/*
 			 * Insert enumerated Security Roles in DB if none exist
 			 */
 			if (SecurityRole.find.findRowCount() == 0) {
 				for (final enums.MyRoles roleEnum : enums.MyRoles.values()) {
 					final SecurityRole role = new SecurityRole();
 					role.setName(roleEnum);
 					role.save();
 				}
 			}
 
 			if (User.find.findRowCount() == 0) {
 
 				// Adding a member
 				PersonBean person = new PersonBean();
 				person.setFirstname("First");
 				person.setLastname("Member");
 				person.setBirthdate(DateTime.parse("1950-01-01 12:00:00"));
 
 				CityBean birthplace = new CityBean();
 				birthplace.setName("Trento");
 
 				person.setBirthplace(birthplace);
 
 				MyUsernamePasswordAuthUser auth = null;
 
 				auth = new MyUsernamePasswordAuthUser("First Member", person,
 						null, "first@example.com", "password",
 						"http://s3.amazonaws.com/37assets/svn/765-default-avatar.png");
 				
 				User user;
 				user = models.User.create(auth);
 				user.setUsernameVerified(true);
 				user.setActive(true);
 				user.setEmailValidated(true);
 				user.update();
 				// TODO add one complete timeline, with stories and mementos
 				
 				LifeStory s = new LifeStory();
 				s.setContributorId(user.getUserId());
 				s.setLocale(user.getLocale());
 				s.setHeadline("Il mio viaggio a Francia");
 				s.setText("È stata la prima volta che sono mai uscito dall'Italia, e anche il viaggio più bello della mia vita");
 				
 				Location loc = new Location();
				loc.setCityName("Parigi");
 				loc.setCountry("Francia");
 				loc.setRegion("");
 				s.setLocation(loc);
 				
 				FuzzyDate start = new FuzzyDate();
 				FuzzyDate end = new FuzzyDate();
 				
 				try {
 					start.setExactDate(new DateTime("1980-09-02"));
 					end.setExactDate(new DateTime("1980-09-08"));
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				s.setStartDate(start);
 				s.setEndDate(end);
 				
 				List<Participation> pList = new ArrayList<Participation>();
 				Participation p = new Participation();
 				p.setContributorId(user.getUserId());
 				p.setPerson(user.getPerson());
 				p.setLifeStory(s);
 				p.setProtagonist(true);
 				pList.add(p);
 				s.setParticipationList(pList);
 				
 				Memento m = new Memento();
 				m.setCategory("PICTURES");
 				m.setHeadline("Torre Eiffel");
 				m.setText("Serata un po' bruta, pero bella da vedere comunque");
 				m.setLocation(loc);
 				m.setStartDate(start);
 				m.setLifeStory(s);
 				m.setIsCover(true);
 				m.setUrl("http://data.whicdn.com/images/29636378/city-paris-photografy-torre-eiffel-vintage-Favim.com-401868_large.jpg");
 				m.setType("IMAGE");
 				List<Memento> mList = new ArrayList<Memento>();
 				mList.add(m);
 				mList.add(m);
 				
 				LifeStory.create(s);
 				s.refresh();
 			}
 		}
 	}
 
 	public void onStart(final Application app) {
 
 		InitialData.insert(app);
 
 		PlayAuthenticate.setResolver(new Resolver() {
 			@Override
 			public Call login() {
 				// Your login page
 				return routes.Application.index();
 			}
 
 			@Override
 			public Call afterAuth() {
 				// The user will be redirected to this page after authentication
 				// if no original URL was saved
 
 				// play.mvc.Call call = new Call() {
 				// @Override
 				// public String url() {
 				// final play.mvc.Http.Cookie cookie = play.mvc.Http.Context
 				// .current().request().cookie("PLAY_SESSION");
 				// String url_decoded = "";
 				// try {
 				// url_decoded = java.net.URLDecoder.decode(
 				// controllers.routes.Application.id(
 				// "!#" + cookie.name() + "="
 				// + cookie.value()).url(),
 				// "UTF-8");
 				// } catch (UnsupportedEncodingException e) {
 				// e.printStackTrace();
 				// }
 				// return url_decoded;
 				// }
 				//
 				// @Override
 				// public String method() {
 				// return "GET";
 				// }
 				// };
 				// return call;
 				return routes.Restricted.index();
 			}
 
 			@Override
 			public Call afterLogout() {
 				// TODO what should we do ?
 				return routes.Application.index();
 			}
 
 			@Override
 			public Call auth(final String provider) {
 				// You can provide your own authentication implementation,
 				// however the default should be sufficient for most cases
 				// return
 				// com.feth.play.module.pa.controllers.routes.Authenticate
 				// .authenticate(provider);
 				return controllers.routes.AuthenticateLocal
 						.authenticate(provider);
 			}
 
 			@Override
 			public Call onException(final AuthException e) {
 				if (e instanceof AccessDeniedException) {
 					return routes.UserControl
 							.oAuthDenied(((AccessDeniedException) e)
 									.getProviderKey());
 				}
 
 				// more custom problem handling here...
 
 				return super.onException(e);
 			}
 
 			@Override
 			public Call askLink() {
 				// We don't support moderated account linking in this sample.
 				// See the play-authenticate-usage project for an example
 				return null;
 			}
 
 			@Override
 			public Call askMerge() {
 				// We don't support moderated account merging in this sample.
 				// See the play-authenticate-usage project for an example
 				return null;
 			}
 		});
 
 		Formatters
 				.register(
 						DateTime.class,
 						new Formatters.AnnotationFormatter<utils.JodaDateTime, DateTime>() {
 							@Override
 							public DateTime parse(
 									utils.JodaDateTime annotation,
 									String input, Locale locale)
 									throws ParseException {
 								if (input == null || input.trim().isEmpty())
 									return null;
 
 								if (annotation.format().isEmpty())
 									return new DateTime(Long.parseLong(input));
 								else
 									return DateTimeFormat
 											.forPattern(annotation.format())
 											.withLocale(locale)
 											.parseDateTime(input);
 							}
 
 							@Override
 							public String print(utils.JodaDateTime annotation,
 									DateTime time, Locale locale) {
 								if (time == null)
 									return null;
 
 								if (annotation.format().isEmpty())
 									return time.getMillis() + "";
 								else
 									return time.toString(annotation.format(),
 											locale);
 							}
 
 						});
 
 	}
 
 }
