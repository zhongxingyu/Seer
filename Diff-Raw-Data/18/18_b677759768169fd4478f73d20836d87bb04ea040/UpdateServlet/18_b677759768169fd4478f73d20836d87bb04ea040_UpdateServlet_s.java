 package com.vaguehope.lookfar.servlet;
 
 import static com.vaguehope.lookfar.servlet.ServletHelper.validateStringParam;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Maps;
 
 public class UpdateServlet extends HttpServlet {
 
 	public static final String CONTEXT = "/update";
 
 	private static final long serialVersionUID = 1157053289236694746L;
 	private static final Logger LOG = LoggerFactory.getLogger(UpdateServlet.class);
 
 	private static final String PARAM_NODE = "node";
 
 	@Override
 	protected void doPost (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		String node = validateStringParam(req, resp, PARAM_NODE);
 		if (node == null) return;
 
		Map<String, String> params = toStringMap(req.getParameterMap());
		LOG.info("TODO update '{}': {}", node, params);
	}

	private static Map<String, String> toStringMap (Map<?,?> in) {
		HashMap<String, String> ret = Maps.newHashMap();
		for (Entry<?, ?> e : in.entrySet()) {
			ret.put(e.getKey().toString(), e.getValue().toString());
 		}
		return ret;
 	}

 }
