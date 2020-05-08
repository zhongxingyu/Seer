 package com.yy.app;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.PathSegment;
 import javax.ws.rs.core.UriInfo;
 
 import org.codehaus.jackson.map.annotate.JsonFilter;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.NotFoundException;
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyService;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.annotation.Index;
 import com.googlecode.objectify.annotation.OnLoad;
 import com.googlecode.objectify.annotation.OnSave;
 import com.googlecode.objectify.cmd.Query;
 import com.googlecode.objectify.condition.IfNotNull;
 import com.sun.jersey.api.view.Viewable;
 import com.yy.app.auth.NotLoginException;
 import com.yy.app.auth.Role;
 import com.yy.app.auth.User;
 import com.yy.app.site.Profile;
 import com.yy.app.tag.Tag;
 import com.yy.rs.Caps;
 import com.yy.rs.Required;
 import com.yy.rs.TagAttr;
 import com.yy.rs.Uniques;
 
 @JsonFilter(value = "RoleBasedFilter")
 public class AModel {
 	protected static final Logger log = Logger
 			.getLogger(AModel.class.getName());
 
 	@Id
 	@Index
 	public Long ID;
 
 	@Index(IfNotNull.class)
 	public Long parent;
 
 	@Index(IfNotNull.class)
 	public Long author;
 
 	@Index(IfNotNull.class)
 	public Long lastModifier;
 
 	public String thumbnail;
 
 	@Index(IfNotNull.class)
 	public Date modified;
 
 	public Date created;
 
 	@Index
 	public Set<Long> attrs;
 
 	@Index(IfNotNull.class)
 	public Boolean __tester;
 
 	public List<? extends AModel> getChildren() {
 		return ObjectifyService.ofy().load().type(this.getClass())
 				.filter("parent", ID).list();
 	}
 
 	public AModel parentModel() {
 		try {
 			if (parent != null && parent != 0)
 				return ObjectifyService.ofy().load().key(Key.create(this.getClass(), parent)).get();
 		} catch (NotFoundException e) {
 			return null;
 		}
 		return null;
 	}
 
 	public boolean isNew() {
 		return this.ID == null || this.ID == 0;
 	}
 
 	@OnSave
 	protected void prePersist() {
 		this.modified = new Date();
 		if (this.author == null)
 			this.author = User.getCurrentUserID();
 
 		checkRequired();
 
 		checkUnique();
 
 		if (this.ID == null || this.ID == 0) {
 			this.created = this.modified;
 		} else {
 			this.lastModifier = User.getCurrentUserID();
 		}
 	}
 
 	@OnLoad
 	protected void postLoad() {
 		populateTagAttr();
 	}
 
 	protected void populateTagAttr() {
 		if (attrs == null || attrs.isEmpty())
 			return;
 		try {
 			Tag.View tagger = Profile.I.tagger;
 			String typeName = this.entityType();
 			for (Field field : getClass().getFields()) {
 				if (field.isAnnotationPresent(TagAttr.class)) {
 					String catKey = field.getAnnotation(TagAttr.class).value();
 					if (catKey == null || catKey.isEmpty())
 						catKey = typeName + "." + field.getName();
 					Tag cat = tagger.get(catKey);
 					if (cat.included.isEmpty())
 						continue;
 
 					List<Long> values = new ArrayList<Long>(attrs);
 					values.retainAll(cat.included);
 					if (values.isEmpty())
 						continue;
 
 					if (Collection.class.isAssignableFrom(field.getType())) {
 						field.set(this, values);
 					} else {
 						field.set(this, values.get(0));
 					}
 				}
 			}
 		} catch (Exception e) {
 			throw new RuntimeException(e.getMessage(), e);
 		}
 	}
 
 	protected void logCreate() {
 
 	}
 
 	protected void logModify() {
 
 	}
 
 	private void checkRequired() {
 		Class<? extends AModel> entityClass = this.getClass();
 		List<String[]> allRequired = new ArrayList<String[]>();
 		Required required = null;
 		Class<?> currentClass = entityClass;
 		while (currentClass != null
 				&& AModel.class.isAssignableFrom(currentClass)) {
 			required = currentClass.getAnnotation(Required.class);
 			if (required != null)
 				allRequired.add(required.value());
 			currentClass = currentClass.getSuperclass();
 		}
 
 		if (allRequired.isEmpty())
 			return;
 
 		String nulls = "";
 		for (String[] aRequired : allRequired) {
 			for (String field : aRequired) {
 				try {
 					if (entityClass.getField(field).get(this) == null)
 						nulls += field + ",";
 				} catch (Exception e) {
 					log.warning("Required:" + e.getMessage());
 				}
 			}
 		}
 		if (nulls.length() != 0)
 			throw new RuntimeException("the following fields can't be empty: "
 					+ nulls);
 	}
 
 	protected void remove() {
 		List<? extends AModel> children = getChildren();
 		if (children != null) {
 			for (AModel child : children)
 				child.remove();
 		}
 	}
 
 	public AModel tester() throws InstantiationException,
 			IllegalAccessException {
 		Objectify store = ObjectifyService.ofy();
 		AModel model = store.load().type(this.getClass()).filter("__tester", true).first()
 				.get();
 		if (model == null) {
 			model = this.getClass().newInstance();
 			model = initTester(model);
 			model.__tester = true;
 			store.save().entity(model).now();
 		}
 		return model;
 	}
 
 	protected AModel initTester(AModel tester) {
 		return tester;
 	}
 
 	public void removeTester() {
 		Objectify store = ObjectifyService.ofy();
 		AModel tester = store.load().type(this.getClass()).filter("__tester", true)
 				.first().get();
 		if (tester != null)
 			store.delete().entity(tester).now();
 	}
 
 	public Object fieldOf(String fieldName) throws SecurityException,
 			NoSuchMethodException, IllegalArgumentException,
 			IllegalAccessException, InvocationTargetException {
 		try {
 			Field field = this.getClass().getField(fieldName);
 			return field.get(this);
 		} catch (NoSuchFieldException e) {
 			Method method = this.getClass().getMethod(
 					"get" + (fieldName.charAt(0) + "").toUpperCase()
 							+ fieldName.substring(1), new Class<?>[] {});
 			return method.invoke(this);
 		}
 	}
 
 	/**
 	 * check unique field before any persist
 	 */
 	private void checkUnique() {
 		Class<? extends AModel> entityClass = this.getClass();
 		String type = this.getClass().getSimpleName();
 
 		List<String[]> allUniques = new ArrayList<String[]>();
 		Uniques uniques = null;
 		Class<?> currentClass = entityClass;
 		while (currentClass != null
 				&& AModel.class.isAssignableFrom(currentClass)) {
 			uniques = currentClass.getAnnotation(Uniques.class);
 			if (uniques != null)
 				allUniques.add(uniques.value());
 			currentClass = currentClass.getSuperclass();
 		}
 
 		if (allUniques.isEmpty())
 			return;
 
 		Object value;
 		Objectify store = ObjectifyService.ofy();
 		Query<? extends AModel> query;
 		int count = 0;
 		StringBuilder message = new StringBuilder();
 
 		for (String[] aUniques : allUniques) {
 			for (String unique : aUniques) {
 				query = store.load().type(this.getClass());
 				if (message.length() > 0)
 					message.delete(0, message.length() - 1);
 				message.append(type).append("(");
 				boolean allIsNull = true;
 				for (String field : unique.split("\\+")) {
 					try {
 						value = entityClass.getField(field).get(this);
 					} catch (Exception e) {
 						throw new RuntimeException("Unique field: " + type
 								+ "." + unique + " is not accessible");
 					}
 					message.append(field).append("=").append(value).append(",");
 					if (value != null) {
 						allIsNull = false;
						query=query.filter(field, value);
						if(query.count()==0)
							break;
 					}
 				}
 				message.deleteCharAt(message.length() - 1).append(")");
 				if (allIsNull)// ignore null;
 					continue;
 				count = query.count();
 
 				if (this.ID == null || this.ID == 0) {
 					if (count > 0)
 						throw new RuntimeException(
 								message.append(
 										" already exists, please change this one or update the existing one")
 										.toString());
 				} else {
 					if (count > 1)
 						throw new RuntimeException(
 								message.append(
 										" already exists, please change this one or update the existing one")
 										.toString());
 				}
 
 			}
 		}
 	}
 
 	public String entityType() {
 		Class<?> c = this.getClass();
 		Entity entity = c.getAnnotation(Entity.class);
 		if (entity != null && entity.name() != null)
 			return entity.name();
 		return c.getSimpleName();
 	}
 
 	private static final DateFormat dateParser = new SimpleDateFormat(
 			"yyyy-M-d");
 	private static final DateFormat datetimeParser = new SimpleDateFormat(
 			"yyyy-M-d-H-m");
 
 	public static class View {
 		protected String template="/index.html";
 		private String path = null;
 
 		public Date parseDate(String date) {
 			if (date == null || date.trim().length() == 0)
 				return null;
 			try {
 				if (date.split("-").length > 3) {
 					return datetimeParser.parse(date);
 				}
 				return dateParser.parse(date);
 			} catch (ParseException e) {
 				throw new RuntimeException(e.getMessage());
 			}
 		}
 
 		public Long parse(long value) {
 			return value == 0 ? null : value;
 		}
 
 		public Float parse(float value) {
 			return value == 0 ? null : value;
 		}
 
 		public Integer parse(int value) {
 			return value == 0 ? null : value;
 		}
 
 		public String parse(String value) {
 			if (value != null && value.trim().length() == 0)
 				return null;
 			return value;
 		}
 
 		public List<String> parseList(String values) {
 			List<String> list = new ArrayList<String>();
 			for (String a : values.split("\n|,")) {
 				a = a.trim();
 				if (a.length() == 0)
 					continue;
 				list.add(a);
 			}
 			return list;
 		}
 
 		public Set<String> parseSet(String values) {
 			if (values == null || values.isEmpty())
 				return null;
 			Set<String> set = new TreeSet<String>();
 			for (String a : values.split("\n|,")) {
 				a = a.trim();
 				if (a.length() == 0)
 					continue;
 				set.add(a);
 			}
 			return set;
 		}
 
 		public AModel newInstance() {
 			try {
 				return (AModel) this.getClass().getEnclosingClass()
 						.newInstance();
 			} catch (Exception e) {
 				return null;
 			}
 		}
 
 		@SuppressWarnings("unchecked")
 		protected String path() {
 			if (this.path != null)
 				return this.path;
 			@SuppressWarnings("rawtypes")
 			Class clazz = this.getClass();
 			Path path = null;
 			while ((path = (Path) clazz.getAnnotation(Path.class)) == null)
 				clazz = clazz.getSuperclass();
 			if (path != null)
 				this.path = path.value();
 			return this.path;
 		}
 
 		protected Map<String, Object> viewDataModel(String title,
 				String contentMacroName, Object... infos) {
 			Map<String, Object> info = new HashMap<String, Object>();
 			if (title != null)
 				info.put("titleContent", title);
 			if (contentMacroName != null)
 				info.put("contentMacroName", contentMacroName);
 
 			if (infos != null && infos.length % 2 == 0) {
 				for (int i = 0; i < infos.length; i++)
 					info.put((String) infos[i], infos[++i]);
 			}
 			return info;
 		}
 
 		protected Viewable viewable(String tmpl, Map<String, Object> info) {
 			return new Viewable("/" + this.path() + "/" + tmpl, info);
 		}
 
 		protected Viewable viewable(Map<String, Object> info) {
 			return new Viewable("/" + this.path() + template, info);
 		}
 
 		@POST
 		@Path("thumbnail")
 		@Produces(MediaType.TEXT_PLAIN)
 		public String updateThumbnail(@FormParam("ID") long ID,
 				@FormParam("thumbnail") String thumbnail) {
 			AModel model = get(ID);
 			model.thumbnail = thumbnail;
 			ObjectifyService.ofy().save().entity(model).now();
 			return model.thumbnail;
 		}
 
 		@GET
 		@Path("{ID:\\d+}")
 		@Produces({ MediaType.APPLICATION_JSON })
 		public AModel get(@PathParam("ID") long ID) {
 			return (AModel) ObjectifyService.ofy().load()
 				.type(this.getClass().getEnclosingClass())
 				.id(ID).get();
 		}
 		
 		public AModel get(String filter, Object value){
 			return (AModel) ObjectifyService.ofy().load()
 				.type(this.getClass().getEnclosingClass())
 				.filter(filter, value).first().get();
 		}
 
 		@POST
 		@Path("remove/{ID:\\d+}")
 		@Produces({ MediaType.APPLICATION_JSON })
 		@Caps("Administrator")
 		public boolean remove(@PathParam("ID") long ID) {
 			if (ID == 0)
 				return false;
 			checkRemoveCapability(ID);
 			AModel model = this.get(ID);
 			model.remove();
 			return true;
 		}
 
 		protected void checkRemoveCapability(long ID) {
 			Role.requestAdmin();
 		}
 
 		@SuppressWarnings("unchecked")
 		@GET
 		@Path("list")
 		@Produces({ MediaType.APPLICATION_JSON })
 		public List<? extends AModel> list(@Context UriInfo uriInfo) {
 			return getListSearchFilter(uriInfo,
 					this.getClass().getEnclosingClass()).list();
 		}
 
 		protected SearchFilter getListSearchFilter(UriInfo uriInfo,
 				Class<?> type) {
 			SearchFilter search = this.extractFilter(uriInfo, type);
 
 			AModel model = this.newInstance();
 			if (model.parent != null)// only list top models
 				search.addFilter("parent", 0);
 
 			return search;
 		}
 
 		@GET
 		@Path("get/{IDs:\\d+(,\\d+)*}")
 		@Produces(MediaType.APPLICATION_JSON)
 		public Collection<?> list(@PathParam("IDs") String IDs) {
 			List<Long> IDList = new ArrayList<Long>();
 			for (String ID : IDs.split(","))
 				IDList.add(Long.parseLong(ID));
 
 			Collection<?> list = ObjectifyService.ofy().load().type(this.getClass().getEnclosingClass())
 				.ids(IDList).values();
 			return list;
 		}
 
 		public AModel get(Objectify store, long ID) {
 			try {
 				return (AModel) (ID == 0 ? this.getClass().getEnclosingClass()
 						.newInstance() : store.load().type(this.getClass().getEnclosingClass()).id(ID).get());
 			} catch (Exception e) {
 				return null;
 			}
 		}
 
 		protected Map<String, String> getParameters(HttpServletRequest req,
 				String... names) {
 			Map<String, String> input = new HashMap<String, String>();
 			if (names != null && names.length > 0) {
 				for (String name : names)
 					input.put(name, req.getParameter(name));
 			} else {
 				String name;
 				@SuppressWarnings("rawtypes")
 				Enumeration e = req.getParameterNames();
 				while (e.hasMoreElements()) {
 					input.put(name = e.nextElement().toString(),
 							req.getParameter(name));
 				}
 			}
 			return input;
 		}
 
 		@GET
 		@Path("{template:.*\\.htm.*}")
 		@Produces({ MediaType.TEXT_HTML })
 		public Viewable direct2Template(@Context HttpServletRequest req) {
 			if(req.getParameter("a")!=null){
 				if(!User.getCurrentUser().isLoggedIn())
 					throw new NotLoginException(req.getRequestURI());
 			}
 			return new Viewable(req.getRequestURI());
 		}
 
 		@Path("itsnotexisting")
 		public Object sub() {
 			return new Object();
 		}
 
 		public SearchFilter extractFilter(UriInfo uriInfo, Class<?> type) {
 			SearchFilter filter = new SearchFilter(type);
 			List<PathSegment> paths = uriInfo.getPathSegments();
 			int len = 0;
 			if (paths == null || (len = paths.size()) == 0)
 				return filter;
 			PathSegment last = paths.get(len - 1);
 			MultivaluedMap<String, String> matrixParams = last
 					.getMatrixParameters();
 			if (matrixParams == null || matrixParams.isEmpty())
 				return filter;
 
 			filter.orderField = matrixParams.getFirst("o");
 
 			String bookmark = matrixParams.getFirst("b");
 			if (bookmark != null && !bookmark.isEmpty())
 				filter.bookmark = Long.parseLong(bookmark);
 
 			String limit = matrixParams.getFirst("l");
 			if (limit != null && !limit.isEmpty())
 				filter.size = Integer.parseInt(limit);
 
 			String t = matrixParams.getFirst("t");
 			if (t != null && !t.isEmpty())
 				filter.title = t;
 
 			for (String key : matrixParams.keySet()) {
 				if ("o".equals(key) || "b".equals(key) || "t".equals(key))
 					continue;
 				String value = matrixParams.getFirst(key);
 				if (value != null && value.length() > 0) {
 					if (key.endsWith("*")) {
 						key = key.substring(0, key.length() - 1);
 						filter.addFilter(key + " >=",
 								filter.typedValue(key, value));
 						filter.addFilter(key + " <", "\uFFFD");
 					} else if (key.endsWith("!")) {
 						key = key.substring(0, key.length() - 1);
 						filter.addFilter(key + " !=",
 								filter.typedValue(key, value));
 					} else
 						filter.addFilter(key, filter.typedValue(key, value));
 				}
 			}
 			return filter;
 		}
 
 		@GET
 		@Path("suggest")
 		public Viewable suggest(@Context UriInfo uriInfo) {
 			SearchFilter filter=this.extractFilter(uriInfo, this.getClass().getEnclosingClass());
 			List<?> models=filter.list();
 			return new Viewable("/empty_template.html",viewDataModel(
 					"","suggest",
 					"template",	"empty_template",
 					"it",models));
 		}
 	}
 }
