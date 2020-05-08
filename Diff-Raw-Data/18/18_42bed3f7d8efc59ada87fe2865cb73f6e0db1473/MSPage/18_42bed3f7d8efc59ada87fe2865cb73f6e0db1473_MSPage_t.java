 /**
  * 
  */
 
 package com.sickle.medu.ms.client.iportal.page;
 
 import com.google.gwt.core.client.Callback;
 import com.google.gwt.user.client.History;
 import com.sickle.medu.ms.client.iportal.IPageConst;
 import com.sickle.medu.ms.client.iportal.panel.ClassPanel;
 import com.sickle.medu.ms.client.iportal.panel.FreeProductPanel;
 import com.sickle.medu.ms.client.iportal.panel.MemberPanel;
 import com.sickle.medu.ms.client.iportal.panel.MessagePanel;
 import com.sickle.medu.ms.client.ui.page.AbstractPage;
 import com.sickle.medu.ms.client.ui.panel.MenuPanel;
 import com.sickle.medu.ms.client.util.ScreenUtil;
 import com.smartgwt.client.types.Alignment;
 import com.smartgwt.client.widgets.Canvas;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.VLayout;
 
 /**
  * manage system page
  * 
  * @author chenhao
  * 
  */
 public class MSPage extends AbstractPage
 {
 
 	private static MSPage instance = new MSPage( );
 
 	private MenuPanel wholepanel = null;
 	
 	private ClassPanel classpanel = null;
 	
 	private MemberPanel memberpanel = null;
 	
 	private MessagePanel messagepanel = null;
 
 	public static MSPage getInstance( )
 	{
 		return instance;
 	}
 
 	private MSPage( )
 	{
 		super( IPageConst.PAGE_MS );
 		init( );
 	}
 
 	private void init( )
 	{
 		this.setWidth100( );
 		this.setHeight100( );
 
 		// 上部分
 		this.addMember( getDefaultTopPanel( ) );
 		// 中间部分
 		this.addMember( getMSPanel( ) );
 		// 下部分
 		this.addMember( getDefaultVersionPanel( ) );
 	}
 
 	private Canvas getMSPanel( )
 	{
 		HLayout fprpage = new HLayout( );
 		fprpage.setWidth100( );
 		fprpage.setAlign( Alignment.CENTER );
 		fprpage.setStyleName( "registerpage" );
 
 		wholepanel = new MenuPanel(
 				ScreenUtil.getWidthInt( IPageConst.PAGE_WIDTH ),
 				ScreenUtil.getHeightInt( 0.8 ) );
 		
 		memberpanel = new MemberPanel();
 		classpanel = new ClassPanel();
 		messagepanel = new MessagePanel();
 		
 		wholepanel.addMenu( "个人信息", memberpanel );
 		wholepanel.addMenu( "班级管理", classpanel );
		
 		wholepanel.addMenu( "报名管理", new VLayout() );
 		wholepanel.addMenu( "留言管理", messagepanel );
 		wholepanel.addMenu( "评论管理", new VLayout() );
		
 		wholepanel.addMenu( "免费定制", new FreeProductPanel() );
 		
 		Callback<Object, Object> call = new Callback<Object, Object>( ) {
 			@Override
 			public void onFailure( Object reason )
 			{
 			}
 			@Override
 			public void onSuccess( Object result )
 			{
 				History.newItem( IPageConst.PAGE_MEDU );
 			}
 		};
 		wholepanel.addMenu( "回到主页", call );
 		
 		fprpage.addMember( wholepanel );
 		return fprpage;
 	}
 
 	
 	/**
 	 * @return the wholepanel
 	 */
 	public MenuPanel getWholepanel( )
 	{
 		return wholepanel;
 	}
 
 	
 	/**
 	 * @return the memberpanel
 	 */
 	public MemberPanel getMemberpanel( )
 	{
 		return memberpanel;
 	}
 
 	
 	/**
 	 * @return the messagepanel
 	 */
 	public MessagePanel getMessagepanel( )
 	{
 		return messagepanel;
 	}
 
 }
