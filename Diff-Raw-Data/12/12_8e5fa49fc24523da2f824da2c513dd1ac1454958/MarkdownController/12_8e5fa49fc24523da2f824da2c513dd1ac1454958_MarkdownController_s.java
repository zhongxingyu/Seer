 /**
  * @author hyd
  * @date 2012-11-14 上午9:30:09 
  * @version 1.0
  */
 package f.rd.paths.web.controller.md;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.servlet.ServletContext;
 
 import org.apache.commons.io.IOUtils;
 import org.pegdown.PegDownProcessor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.core.io.Resource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.context.ServletContextAware;
 
 
 
 /**
 * <p>
  * Markdown格式的文档
 * </p>
  * 
  * @date 2012-11-14 上午9:30:09
  * 
  */
 @Controller
 public class MarkdownController implements ServletContextAware {
 	
 	private Logger log = LoggerFactory.getLogger(this.getClass());
 	
 	 private ServletContext servletContext;
 	 
 	/**
 	 * 实现ServletContextAware接口，获取本地路径
 	 * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
 	 */
 	public void setServletContext(ServletContext servletContext) {
 		this.servletContext = servletContext;
 	}
 	
 	@RequestMapping(value="/md-docs", method=RequestMethod.GET)
 	public String preface(Model model) {
 		return getInnerHtml("preface", model);
 	}
 
 	/**
 	 * /md-docs/{title}: 会自动截断.md后缀
 	 * @param title
 	 * @param model
 	 * @return
 	 */
 	@RequestMapping(value="/md-docs/{title}", method=RequestMethod.GET)
 	public String title(@PathVariable String title, Model model) {
 		if ("".equals(title.trim())) {
 			title = "preface";
 		}
 		return getInnerHtml(title, model);
 	}
 	
 	/**
 	 * 加载md文档，转换成html模式，输出到页面
 	 * @param title
 	 * @param model
 	 * @return
 	 */
 	private String getInnerHtml(String title, Model model) {
 		log.info("工程Markdown格式文档: {}", title + ".md");
 		String mdSource = "";
 		try {
 			// 获取本地存储路径
 			String localPath = this.servletContext.getRealPath(File.separator + "resources" + File.separator + "md-docs");
 			Resource rs = new FileSystemResource(localPath + File.separator + title + ".md");
 			mdSource = IOUtils.toString(rs.getInputStream());
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			mdSource = "**未到找文件：" + title + ".md**";
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		// 转到html格式
 		PegDownProcessor peg = new PegDownProcessor();
 		model.addAttribute("innerHtml", peg.markdownToHtml(mdSource));
 		return "/md-docs";
 	}
 
 	
 }
