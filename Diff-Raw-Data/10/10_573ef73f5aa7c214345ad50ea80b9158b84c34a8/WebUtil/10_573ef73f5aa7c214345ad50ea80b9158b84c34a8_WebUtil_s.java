 package com.digt.web;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.concurrent.ExecutionException;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.shindig.auth.SecurityToken;
 import org.apache.shindig.protocol.ProtocolException;
 import org.apache.shindig.protocol.model.FilterOperation;
 import org.apache.shindig.protocol.model.SortOrder;
 import org.apache.shindig.social.opensocial.model.Person;
 import org.apache.shindig.social.opensocial.spi.CollectionOptions;
 import org.apache.shindig.social.opensocial.spi.GroupId;
 import org.apache.shindig.social.opensocial.spi.PersonService;
 import org.apache.shindig.social.opensocial.spi.UserId;
 
 import com.digt.spi.ProfileService;
 import com.digt.model.PersonProfile;
 
 public class WebUtil {
 
 	public static final String APP_PFX = "/web";
     
     public static final String PROFILE_ATTR = "profile_attr";
 	public static final String PERSON_ATTR = "person_attr";
 	public static final String TEMPLATE_ATTR = "act_tpl_attr";
 	public static final String TOKEN_ATTR = "token";
 
 	public static final  String START_INDEX = "startIndex";
 	public static final  String COUNT = "count";
 	public static final  String SORT_BY = "sortBy";
 	public static final  String SORT_ORDER = "sortOrder";
 	public static final  String FILTER_BY = "filterBy";
 	public static final  String FILTER_OPERATION = "filterOp";
 	public static final  String FILTER_VALUE = "filterValue";
 
 	public static final String GROUP_PARAM = "groupId";
 	public static final String FILTER_PARAM = "filter";
 
 	public static final GroupId GROUP_FRIENDS = GroupId.fromJson("@friends");
 	public static final GroupId GROUP_SELF = GroupId.fromJson("@self");
 	public static final GroupId GROUP_ALL = GroupId.fromJson("@all");
 	public static final UserId USER_ME = UserId.fromJson("@me");
 	
 	public final static String MAIL_CHARSET = "UTF-8";
 
 
 	public static PersonProfile getProfileFromRequest(HttpServletRequest req, ProfileService profsvc) 
 			throws ProtocolException, InterruptedException, ExecutionException {
 
 		//HttpSession sess = req.getSession();
 		//PersonProfile profile = (PersonProfile)sess.getAttribute(PROFILE_ATTR);
 		//if (profile == null)
 		//{
 		SecurityToken token = (SecurityToken) req.getAttribute(WebUtil.TOKEN_ATTR);
 		PersonProfile profile = profsvc.getPersonProfileId(token.getViewerId()).get();
 		//sess.setAttribute(PROFILE_ATTR, profile);
 		//}
 
 		return profile;
 	}
 
 	public static Person getPersonFromRequest(HttpServletRequest req, PersonService svc) 
 			throws ProtocolException, InterruptedException, ExecutionException {
 
 		SecurityToken token = (SecurityToken) req.getAttribute(TOKEN_ATTR);
 		String uid = token.getViewerId();
 		UserId userId = new UserId(UserId.Type.userId, uid);
 		Person person = svc.getPerson(userId, null, token).get();
 
 		return person;
 	}
 
 	/*
 	public static Activity getActivityTemplate(HttpServletRequest req, PersonService svc) 
 			throws ProtocolException, InterruptedException, ExecutionException {
 		HttpSession sess = req.getSession();
 		ActivityDb res = (ActivityDb)sess.getAttribute(TEMPLATE_ATTR);
 		if (res == null)
 		{
 			SecurityToken token = (SecurityToken) req.getAttribute(TOKEN_ATTR);
 			UserId userId = new UserId(UserId.Type.me, null);
 			Person person = svc.getPerson(userId, null, token).get();
 			Map<String, String> template = new HashMap<String, String>();
 			template.put("PersonKey", person.getName().getFormatted());
 			res = new ActivityDb();
 			res.setTemplateParams(template);
 			// TODO: add user photo here
 			//MediaItemDb mItem = new MediaItemDb();
 			//mItem.setThumbnailUrl(person.getThumbnailUrl());
 			//mItem.setUrl(person.getThumbnailUrl());
 			//mItem.setType(Type.IMAGE);
 			//res.setMediaItems((List)Arrays.asList(mItem));
 			sess.setAttribute(TEMPLATE_ATTR, res);
 		}
 
 		return res;
 	}
 	*/
 	public static String getCookie(HttpServletRequest req, String name)
 	{
 		String cookie = null;
 		if (req.getCookies() != null)
 		{
 			for (Cookie c : req.getCookies())
 			{
 				if (c.getName().equals(name))
 					return c.getValue();
 			}
 		}
 		return cookie;
 	}
 
 	public static GroupId getGroup(HttpServletRequest request)
 	{
 		String group = request.getParameter(GROUP_PARAM);
 		if (group != null)
 		{
 			return GroupId.fromJson(group);
 		}
 
 		return GROUP_SELF;
 	}
 
 	public static CollectionOptions getOptions(HttpServletRequest request)
 	{
 		CollectionOptions res = new CollectionOptions();
 		res.setFilter(request.getParameter(FILTER_BY));
 		if (request.getParameter(FILTER_OPERATION) != null)
 			res.setFilterOperation(FilterOperation.valueOf(request.getParameter(FILTER_OPERATION)));
 		res.setFilterValue(request.getParameter(FILTER_VALUE));
 		res.setSortBy(request.getParameter(SORT_BY));
 		if (request.getParameter(SORT_ORDER) != null)
 			res.setSortOrder(SortOrder.valueOf(request.getParameter(SORT_ORDER)));
 		if (request.getParameter(START_INDEX) != null)
 			res.setFirst(Integer.valueOf(request.getParameter(START_INDEX)));
		if (request.getParameter(COUNT) != null)
 			res.setMax(Integer.valueOf(request.getParameter(COUNT)));
 		return res;
 	}
 	
 	public static String readTemplate(BufferedReader in) 
 			throws IOException {
 		StringBuilder text = new StringBuilder();
 		String s;
 		while ((s = in.readLine()) != null) {
 			text.append(s).append("\n");
 		}
 		in.close();
 		return text.toString();
 	}
     
     public static boolean isAjaxRequest(HttpServletRequest req) {
 		return "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));
     }
 }
