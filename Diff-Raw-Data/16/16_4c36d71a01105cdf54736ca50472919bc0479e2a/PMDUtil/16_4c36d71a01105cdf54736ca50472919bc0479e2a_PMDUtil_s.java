 package com.jcpa.util;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import net.sourceforge.pmd.PMD;
 import net.sourceforge.pmd.PMDConfiguration;
 import net.sourceforge.pmd.Rule;
 import net.sourceforge.pmd.RuleContext;
 import net.sourceforge.pmd.RuleSet;
 import net.sourceforge.pmd.RuleSetFactory;
 import net.sourceforge.pmd.RuleSets;
 import net.sourceforge.pmd.RulesetsFactoryUtils;
 import net.sourceforge.pmd.benchmark.Benchmark;
 import net.sourceforge.pmd.benchmark.Benchmarker;
 import net.sourceforge.pmd.lang.Language;
 import net.sourceforge.pmd.lang.LanguageVersion;
 import net.sourceforge.pmd.lang.LanguageVersionDiscoverer;
 import net.sourceforge.pmd.renderers.Renderer;
 import net.sourceforge.pmd.util.IOUtil;
 import net.sourceforge.pmd.util.datasource.DataSource;
 
 public class PMDUtil{
 	/**
 	 * 对源代码产生分析报告，保存到文件中
 	 * @param	InputPaths	String	源代码目录或文件路径
 	 * @param	RuleSets	String	规则集xml路径
 	 * @param	ReportFile	String	报告保存路径
 	 * @param	ReportFormat	String	报告生成形式（xml,csv,html,emacs,ideaj,summaryhtml,xslt,vbhtml,text,textpad,textcolor,yahtml）
 	 * */
 	public static boolean report(String InputPaths,String RuleSets,String ReportFile,String ReportFormat){
 		//设置配置参数
 		PMDConfiguration configuration=new PMDConfiguration();
 		configuration.setInputPaths(InputPaths);//源代码目录或文件路径
 		configuration.setRuleSets(RuleSets);//规则集xml路径
 		configuration.setReportFile(ReportFile);//报告保存路径
 		configuration.setReportFormat(ReportFormat);//报告生成形式（xml,csv）
 		
 		// Load the RuleSets
 		long startLoadRules = System.nanoTime();
 		RuleSetFactory ruleSetFactory = RulesetsFactoryUtils.getRulesetFactory(configuration);
 		RuleSets ruleSets = RulesetsFactoryUtils.getRuleSets(configuration.getRuleSets(), ruleSetFactory, startLoadRules);
 		if (ruleSets == null)return false;
 		//根据规则集设置语言
 		Set<Language> languages = new HashSet<Language>();
 		LanguageVersionDiscoverer discoverer = configuration.getLanguageVersionDiscoverer();
 		for (Rule rule : ruleSets.getAllRules()) {
 			Language language = rule.getLanguage();
 			if (languages.contains(language))continue;
 			LanguageVersion version = discoverer.getDefaultLanguageVersion(language);
 			if (RuleSet.applies(rule, version)) {
 				languages.add(language);
 			}
 		}
 		
 		List<DataSource> files = PMD.getApplicableFiles(configuration,languages);
 
 		try {
 			Renderer renderer = configuration.createRenderer();
 			List<Renderer> renderers = new LinkedList<Renderer>();
 			renderers.add(renderer);
 
 			renderer.setWriter(IOUtil.createWriter(configuration.getReportFile()));
 			renderer.start();
 			
 			RuleContext ctx = new RuleContext();
 
 			PMD.processFiles(configuration, ruleSetFactory, files, ctx, renderers);
 
 			renderer.end();
 			renderer.flush();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	
 	/**
 	 * 对源代码产生分析报告，产生的报告由renderer进行处理
 	 * @param	InputPaths	String	源代码目录或文件路径
 	 * @param	RuleSets	String	规则集xml路径
 	 * @param	renderer	Renderer
 	 * */
 	public static boolean report(String InputPaths,String RuleSets,Renderer renderer){
 		//设置配置参数
 		PMDConfiguration configuration=new PMDConfiguration();
 		configuration.setInputPaths(InputPaths);//源代码目录或文件路径
 		configuration.setRuleSets(RuleSets);//规则集xml路径
 		
 		// Load the RuleSets
 		long startLoadRules = System.nanoTime();
 		RuleSetFactory ruleSetFactory = RulesetsFactoryUtils.getRulesetFactory(configuration);
 		RuleSets ruleSets = RulesetsFactoryUtils.getRuleSets(configuration.getRuleSets(), ruleSetFactory, startLoadRules);
 		if (ruleSets == null)return false;
 		//根据规则集设置语言
 		Set<Language> languages = new HashSet<Language>();
 		LanguageVersionDiscoverer discoverer = configuration.getLanguageVersionDiscoverer();
 		for (Rule rule : ruleSets.getAllRules()) {
 			Language language = rule.getLanguage();
 			if (languages.contains(language))continue;
 			LanguageVersion version = discoverer.getDefaultLanguageVersion(language);
 			if (RuleSet.applies(rule, version)) {
 				languages.add(language);
 			}
 		}
 		
 		List<DataSource> files = PMD.getApplicableFiles(configuration,languages);
 
 		long reportStart = System.nanoTime();
 		try {
 			List<Renderer> renderers = new LinkedList<Renderer>();
 			renderers.add(renderer);
 
 			renderer.start();
 			
 			Benchmarker.mark(Benchmark.Reporting, System.nanoTime() - reportStart, 0);
 			
 			RuleContext ctx = new RuleContext();
 
 			PMD.processFiles(configuration, ruleSetFactory, files, ctx, renderers);
 
 			reportStart = System.nanoTime();
 			renderer.end();
 			renderer.flush();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		} finally {
 			Benchmarker.mark(Benchmark.Reporting, System.nanoTime() - reportStart, 0);
 		}
 		return true;
 	}
 	/**
 	 * 对源代码产生分析报告，产生的报告由renderer进行处理
 	 * @param	InputPaths	String	源代码目录或文件路径
 	 * @param	RuleSets	String	规则集xml路径
 	 * @param	renderer	Renderer
 	 * */
 	public static int reportByCmd(String InputPaths,String ReportFmt,String RuleSets){
 		String[] args=new String[3];
 		args[0]=InputPaths;args[1]=ReportFmt;args[2]=RuleSets;
 		return PMD.run(args);
 	}
 	/**
 	 * 通过expression和auxiliary获取完整的expression
 	 * */
 	public static String ExpIntegrate(String exp,String auxStr){
 		if(exp==null || exp.equals(""))return "";
 		if(auxStr==null || auxStr.equals(""))return exp;
 		if(auxStr.startsWith("##"))auxStr=auxStr.substring(2);
 		
 		String auxs[]=auxStr.split("##");
		for(String aux:auxs){
			String items[]=aux.split(",");
			String itemStr="";
 			if(items.length>0){
 				itemStr+="@Image=\""+items[0]+"\"";
 			}
 			for(int i=1;i<items.length;i++){
 				itemStr+=" or @Image=\""+items[i]+"\"";
 			}
			exp=exp.replaceFirst("##AUX##",itemStr);
 		}
 		return exp;
 	}
 }
