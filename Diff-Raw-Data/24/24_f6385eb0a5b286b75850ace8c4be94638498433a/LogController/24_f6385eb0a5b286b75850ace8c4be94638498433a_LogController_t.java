 package com.ocbcmcd.monitoring.web;
 
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 
 import java.text.SimpleDateFormat;
 import java.util.List;
 
import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.beans.support.PagedListHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.ocbcmcd.monitoring.command.LogSearchCommand;
 import com.ocbcmcd.monitoring.domain.LogEvent;
 import com.ocbcmcd.monitoring.query.ILogEventQuery;
 
 @Controller
 @RequestMapping("/log")
 public class LogController {
 	protected Log log = LogFactory.getLog(getClass());
 	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
 
 	@Autowired
 	private ILogEventQuery logQuery;
 
 	@Value("${paging.size}")
 	private String _pageSize;
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@RequestMapping(method = GET)
 	public ModelMap list(@ModelAttribute("command") LogSearchCommand command,
 			@RequestParam(required = false, value = "p") String p) {
 		ModelMap model = new ModelMap();
 		String requestString = "";
 
 		if (command == null) {
 			command = new LogSearchCommand();
 		}
 
 		model.addAttribute("command", command);
 
 		int page = 0;
 		List<LogEvent> searchResults = logQuery.getLogs(command);
 		PagedListHolder pagedListHolder = new PagedListHolder(searchResults);
 
 		try {
 			page = Integer.parseInt(p);
 		} catch (NumberFormatException e) {
 
 		}
 
 		pagedListHolder.setPage(page);
 		int pageSize = Integer.parseInt(_pageSize);
 		pagedListHolder.setPageSize(pageSize);
 
 		model.addAttribute("pagedListHolder", pagedListHolder);
 		
		requestString = generateRequestString(command);
		model.addAttribute("requestString", requestString);
		
		return model;
	}

	private String generateRequestString(LogSearchCommand command) {
		String requestString = "";
 		if (command.getStartDate() != null && command.getEndDate() != null) {
 			requestString = String.format("from=%s&to=%s", dateFormat.format(command.getStartDate()), dateFormat.format(command.getEndDate()));
 		}
 		
		if (!StringUtils.isEmpty(command.getFile())) {
			String file_str = "file=" + command.getFile();
			if (StringUtils.isEmpty(requestString)) {
				requestString = file_str;
			} else {
				requestString += "&" + file_str;
			}
		}
		
		return requestString;
 	}
 
 }
