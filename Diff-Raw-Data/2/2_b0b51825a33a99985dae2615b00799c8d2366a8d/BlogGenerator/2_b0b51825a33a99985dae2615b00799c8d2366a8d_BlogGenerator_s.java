 package de.mactunes.schmitzkatz.blog;
 
 import de.mactunes.schmitzkatz.BlogProperties;
 import de.mactunes.schmitzkatz.blog.posts.BlogPostCreator;
 import de.mactunes.schmitzkatz.blog.posts.BlogPost;
 import org.apache.commons.io.FileUtils;
 import java.io.FileFilter;
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 import freemarker.template.DefaultObjectWrapper;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.Collections;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.HashMap;
 
 
 
 public class BlogGenerator {
 
 	private static final String KEY_POSTS = "blogposts";
 	private static final String KEY_POST = "post";
 	private static final String KEY_MAIN_BLOG_PAGE_TEMPLATE = "blog.ftl"; // TODO get from props
 	private static final String KEY_BLOG_POST_TEMPLATE = "blog-post.ftl"; // TODO get from props
 	private static final String BLOG_MAIN_HTML_FILENAME = "blog.html"; // TODO get from props
 
 	private BlogProperties properties;
 	private Configuration configuration;
 
 
 	public BlogGenerator(BlogProperties properties) {
 		this.properties = properties;
 
 		initializeTemplateEngine();
 	}
 
 	private void initializeTemplateEngine() {
 		configuration = new Configuration();
 		try {
 			configuration.setDirectoryForTemplateLoading(new File(BlogPost.TEMPLATES_PATH));
 			configuration.setObjectWrapper(new DefaultObjectWrapper());
 		} catch (IOException ioe) {
 			System.out.println("Error: Could not load template configuration.");
 		}
 	}
 
 	public void generate() {
 		try { // clean generated directory
 			FileUtils.cleanDirectory(new File(BlogPost.GENERATED_PATH));
 		} catch (IOException ioe) {
 			System.out.println("Error: could not clean the Generated-directory.");
 		}
 
 		List<BlogPost> posts = new LinkedList<BlogPost>();
 		String[] postDirectories = fetchCurrentBlogPostDirectories();
 
 		// add all deserialized posts to list and sort it by publishing-date
 		for (String postDirectory : postDirectories) {
 			BlogPost post = parseJSONInPost(postDirectory);
 			posts.add(post);
 		}
 		Collections.sort(posts, Collections.reverseOrder()); // sort by date
 
 		// renders the main blog-page template
 		injectBlogPostsIntoMainBlogTemplate(posts);
 
 		for (BlogPost post : posts) {
 			String postPath = post.getPostDirInGeneratedDir();
 
 			File postPathFile = new File(postPath);
 			postPathFile.mkdirs();			
 
 			copyMediaDirToGeneratedPost(post);
 
 			injectBlogPostIntoBlogPostTemplate(post);
 		}
 
 		copyRemainingTemplatesToGeneratedDir();
 	}
 
 
 	private void copyRemainingTemplatesToGeneratedDir() {
 		try {
 			FileUtils.copyDirectory(
 				new File(BlogPost.TEMPLATES_PATH), 
 				new File (BlogPost.GENERATED_PATH), 
 				new FileFilter() {
 					@Override
 					public boolean accept(File pathName) {
 						String name = pathName.getName();
 
 						if (name.endsWith(".ftl")) {
 							return false;
 						}
 
 						return true;
 					}
 				}
 			);
 		} catch (IOException ioe) {
 			System.out.println("Error: could not copy remaining files into Generated-directory.");
 		}
 	}
 
 
 	private void injectBlogPostsIntoMainBlogTemplate(List<BlogPost> posts) {
 		// create a root-map which freemarker needs
 		Map<String, List<BlogPost>> root = new HashMap<String, List<BlogPost>>();
 		root.put(KEY_POSTS, posts);
 
 		try {
 			Template blogTemplate = configuration.getTemplate(KEY_MAIN_BLOG_PAGE_TEMPLATE);
 
 			Writer out = new FileWriter(
 				new File(BlogPost.GENERATED_PATH + File.separator + BLOG_MAIN_HTML_FILENAME));
 			blogTemplate.process(root, out);
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			System.out.println("Error: could not process template for main blog-page.");
 			e.printStackTrace();
 		}
 	}
 
 	private void injectBlogPostIntoBlogPostTemplate(BlogPost post) {
 		Map<String, BlogPost> root = new HashMap<String, BlogPost>();
 		root.put(KEY_POST, post);
 
 		try {
 			Template blogpostTemplate = configuration.getTemplate(KEY_BLOG_POST_TEMPLATE);
 
 			Writer out = new FileWriter(
 				new File(post.getPostDirInGeneratedDir() + 
 					File.separator + BlogPost.BLOG_POST_HTML_FILENAME));
 			blogpostTemplate.process(root, out);
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			System.out.println("Error: could not process template for main blog-page.");
 			e.printStackTrace();
 		}
 	} 
 
 
 
 	private void copyMediaDirToGeneratedPost(BlogPost post) {
 		try {
			FileUtils.copyDirectoryToDirectory(new File(post.getPostMediaDir()), 
 												new File(post.getPostMediaDirInGeneratedDir()));
 		} catch (IOException ioe) {
 			System.out.println("Error: Could not copy media dir for post " + post.getIdentifier());
 		}
 	}
 
 
 	private String[] fetchCurrentBlogPostDirectories() {
 		File postsDir = new File(BlogPost.POSTS_PATH);
 
 		return postsDir.list(new FilenameFilter() {
 			@Override
 			public boolean accept(File dir, String name) {
 				return dir.isDirectory();
 			}
 		});
 	}
 
 	private BlogPost parseJSONInPost(String postDirectory) {
 		String postJSONPath = BlogPost.POSTS_PATH + File.separator + 
 							postDirectory + File.separator +
 							BlogPostCreator.BLOG_ENTRY_FILENAME;
 		String postMarkdownPath = BlogPost.POSTS_PATH + File.separator + 
 							postDirectory + File.separator +
 							BlogPostCreator.BLOG_MARKDOWN_FILENAME;
 
 		try {
 			String strJSON = FileUtils.readFileToString(new File(postJSONPath));
 			String strMarkdown = FileUtils.readFileToString(new File(postMarkdownPath));
 
 			return new BlogPost(strJSON, strMarkdown, postDirectory);
 		} catch (IOException ioe) {
 			System.out.println("Error: Failed reading JSON-File for post: \n" + 
 									postJSONPath + "or\n" +
 									"postMarkdownPath");
 		}
 
 		return null;
 	}
 
 
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
