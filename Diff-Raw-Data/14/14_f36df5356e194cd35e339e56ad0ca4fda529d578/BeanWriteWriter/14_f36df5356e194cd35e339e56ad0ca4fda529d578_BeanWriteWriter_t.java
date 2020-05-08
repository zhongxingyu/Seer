 package com.bertvanbrakel.codemucker.bean;
 
 import com.bertvanbrakel.lang.annotation.NotThreadSafe;
 import com.bertvanbrakel.test.bean.BeanDefinition;
 
 @NotThreadSafe
 public class BeanWriteWriter extends AbstractBeanWriter {
 
 	public BeanWriteWriter(String beanClassName) {
 		this(new BeanBuilderOptions(), beanClassName);
 	}
 	
 	public BeanWriteWriter(BeanBuilderOptions options, String beanClassName){
 		super(options, beanClassName + "Write");
 	}
 
 	@Override
 	public void generate(BeanDefinition def){
 		generatePkgDecl();
 		generateImports();
 		generateInterfaceOpen();
 		generateInterfaceSetters(def);
	//	generateFieldEquals(def);
		//generateFieldClone(def);
 		generateClassClose();	
 	}
 
 }
