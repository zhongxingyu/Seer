 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.flex2.rpc.remoting.service.fds2.factory;
 
 
 
 import javax.servlet.ServletConfig;
 
 import org.seasar.flex2.rpc.remoting.service.RemotingServiceLocator;
 import org.seasar.flex2.rpc.remoting.service.impl.RemotingServiceInvokerImpl;
 import org.seasar.framework.container.S2Container;
 import org.seasar.framework.container.deployer.ComponentDeployerFactory;
 import org.seasar.framework.container.deployer.ExternalComponentDeployerProvider;
 import org.seasar.framework.container.external.servlet.HttpServletExternalContext;
 import org.seasar.framework.container.external.servlet.HttpServletExternalContextComponentDefRegister;
 import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
 
 import flex.messaging.FactoryInstance;
 import flex.messaging.FlexContext;
 import flex.messaging.FlexFactory;
 import flex.messaging.config.ConfigMap;
 import flex.messaging.util.StringUtils;
 
 /**
  * <h4>Seasar2ComponentsFactory</h4>
  * <p>FDS2のFactoryMechanismを利用したFactoryクラスです。
  * Flex2クライアントからFDS2にアクセスした際に
  * S2Containerに登録されたコンポーネントを呼び出すことが可能になります</p>
  * 
  * <p>このfactoryを利用するには、services-config.xmlに以下の記述を追加します。<br />
  * services-config.xmlは、FDS2アプリケーションディレクトリのWEB-INF/flex/以下にあります。</p>
  * <pre>
  *	&lt;factories&gt;
  *		&lt;factory id="s2" class="org.seasar.flex2.rpc.remoting.service.fds2.factory.Seasar2Factory" /&gt;
  *	&lt;/factories&gt;
  * </pre>
  * あわせて、S2Containerを利用するのに必要な設定をweb.xml(WEB-INF/web.xml)に追記します。
  * <pre>
  * 	&lt;filter&gt;
  *		&lt;filter-name&gt;s2filter&lt;/filter-name&gt;
  *		&lt;filter-class>org.seasar.framework.container.filter.S2ContainerFilter&lt;/filter-class&gt;
  *	&lt;/filter&gt;
  *	&lt;filter-mapping&gt;
  *		&lt;filter-name&gt;s2filter&lt;/filter-name&gt;
  *		&lt;url-pattern&gt;/*&lt;/url-pattern&gt;
  *	&lt;/filter-mapping&gt;
  * </pre>
  * Seasar2と連携する為に必要なjarファイルをWEB-INF/lib以下にコピーします。
  * 	<ul>
  *		<li>aopalliance-1.0.jar</li>
  *		<li>geronimo-jta_1.0.1B_spec-1.0.jar</li>
  *		<li>javassist-3.4ga.jar</li>
  *		<li>log4j-1.2.13.jar</li>
  *		<li>ognl-2.6.7.jar</li>
  *		<li>s2-extension-2.4.x.jar(2.4.9以降)</li>
  *		<li>s2-fds-s2factory-xx.jar</li>
  *		<li>s2-framework-2.4.x.jar(2.4.9以降)</li>
  *	</ul>
  *  <p>
  *  ここまでで連携に必要な準備完了です。
  *  あとは呼び出すサービス毎に以下のような記述を、WEB-INF/flex/remoting-config.xmlに追加します。</p>
  *  <pre>
  *	&lt;destination id="addService"&gt;
  *		&lt;properties&gt;
  *			&lt;factory&gt;s2&lt;/factory&gt;
  *		&lt;/properties&gt;
  *	&lt;/destination&gt;
  *  </pre>
  *  <strong>id</strong>のところに、service名を記述します。
  *  @version b3
  *  @author nod
  */
 public class Seasar2Factory extends RemotingServiceInvokerImpl implements
 		FlexFactory {
 
 	/**  source tag  name CONSTANT */
 	private static final String SOURCE = "source";
 
 	/** ServiceLocatorのインスタンス */
 	protected RemotingServiceLocator remotingServiceLocator;
 
 	private static final long serialVersionUID = 407266935204779128L;
 	/** Containerの設定ファイルパスを示す定数 */
     public static final String CONFIG_PATH_KEY = "configPath";
 
 	/**
 	 * このメソッドでは、Factoryの定義を初期化するときに呼ばれます。
 	 * <!--
 	 * This method is called when the definition of an instance that this factory
 	 * looks up is initialized.
 	 * -->
 	 * @param id このファクトリのID
 	 * @param configMap コンポーネントの設定情報
 	 * @return FatoryInstance　指定されたIDに対するFactoryInstance
 	 * 
 	 */
 	public FactoryInstance createFactoryInstance(String id, ConfigMap configMap) {
 
 		FactoryInstance instance = new FactoryInstance(this, id,
 				configMap);
 		instance.setSource(configMap.getPropertyAsString(SOURCE, instance
 				.getId()));
 
 		return instance;
 
 	}
 
 	/**
 	 * flex-services.xmlで設定されているserviceNameのsourceタグで指定された
 	 * サービス名よりS2Containerに登録されているコンポーネントを取得します。
 	 * ロジック実行は、Adapterによって起動されます
 	 * 
	 * @param FactoryInstance FactoryInstance
 	 * @return Object S2Containerより取得したコンポーネント
 	 * <br/>
 	 * <!--
 	 * Returns the instance specified by the source
 	 * and properties arguments. 
 	 * -->
 	 */
 
 	public Object lookup(FactoryInstance factoryInstance) {
 		//if source elements is not found,return the id attribute.
 		//see createFactoryInstance methods.
 		
 		String serviceName = factoryInstance.getSource();
 		final Object service = remotingServiceLocator.getService(serviceName);
 		
 		//limits.
 		// 1.sessionData Import/Export not supported.
 		// 2.argmentAjuster not supported.
 		// 3.remoting service validator not supported.
 		return service;
 	}
 
 	/**
 	 * 
 	 *　設定情報とともにコンポーネントを初期化します。<br />
 	 *  ここではS2Servletが行っているような、S2Containerの初期化を行います。<br/>
 	 *  <!--
 	 * Initializes the component with configuration information.
 	 * -->
 	 * @param  id contains an identity you can use in diagnostic messages to determine which component's configuration this is
 	 * @param configMap  コンポーネントの設定情報 contains the properties for configuring this component.
 	 */
 
 	public void initialize(final String id, final ConfigMap configMap) {
 
 		//S2Container intialize sequence.
 		String configPath = null;
 
 		ServletConfig servletConfig =FlexContext.getServletConfig();
 		
         if (servletConfig != null) {
             configPath = servletConfig.getInitParameter(CONFIG_PATH_KEY);    
         }
         
         if (!StringUtils.isEmpty(configPath)) {
             SingletonS2ContainerFactory.setConfigPath(configPath);
         }
         
         /* ここからSingletonContainerInitializerのかわり */
         if (ComponentDeployerFactory.getProvider() instanceof ComponentDeployerFactory.DefaultProvider) {
             ComponentDeployerFactory
                     .setProvider(new ExternalComponentDeployerProvider());
         }
         
         HttpServletExternalContext extCtx = new HttpServletExternalContext();
         extCtx.setApplication(FlexContext.getServletContext());
         
         
         SingletonS2ContainerFactory.setExternalContext(extCtx);
         SingletonS2ContainerFactory
                 .setExternalContextComponentDefRegister(new HttpServletExternalContextComponentDefRegister());
         SingletonS2ContainerFactory.init();
         /* ここまでSingletonContainerInitializerのかわり */
 
 		S2Container container = SingletonS2ContainerFactory.getContainer();
 		remotingServiceLocator = (RemotingServiceLocator) container
 				.getComponent(RemotingServiceLocator.class);
 	}
 
 }
