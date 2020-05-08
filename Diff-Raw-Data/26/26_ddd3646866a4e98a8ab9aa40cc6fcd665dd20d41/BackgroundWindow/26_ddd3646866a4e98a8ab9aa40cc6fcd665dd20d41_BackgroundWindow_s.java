 package org.myftp.gattserver.grass3.windows.template;
 
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.themes.Runo;
 
 public abstract class BackgroundWindow extends GrassWindow {
 
 	private static final long serialVersionUID = 2474374292329895766L;
 
 	protected void buildLayout() {
 
 		// main
 		VerticalLayout mainLayout = new VerticalLayout();
 		mainLayout.setSizeFull(); // to ensure whole space is in use
 		setContent(mainLayout);
 
		// header
		VerticalLayout header = new VerticalLayout();
		initHeader(header);

 		// body
 		Panel content = new Panel();
 		content.setCaption(null);
 		// to ensure the panel only takes the available space
 		content.setSizeFull();
 		content.addStyleName("background_layout");
 		((VerticalLayout) content.getContent()).setMargin(false);
 		content.addStyleName(Runo.PANEL_LIGHT); // no borders
 
 		final VerticalLayout contentLayout = new VerticalLayout();
 		// to ensure a scrollbar appears if the content won't fit otherwise
 		contentLayout.setSizeUndefined();
 		contentLayout.setWidth("100%");
 		content.addComponent(contentLayout);
		buildBody(contentLayout);
 
 		// footer
 		VerticalLayout footer = new VerticalLayout();
 		buildFooter(footer);
 
 		// parts composition
		mainLayout.addComponent(header);
 		mainLayout.addComponent(content);
 		mainLayout.addComponent(footer);
 
 		// to determine which component takes the excess space
 		mainLayout.setExpandRatio(content, 1);
 
 	}
 
 	private void initHeader(VerticalLayout layout) {
 
 		layout.setStyleName("layout");
 		layout.setWidth("100%");
 
 		HorizontalLayout headerLayout = new HorizontalLayout();
 		layout.addComponent(headerLayout);
 		layout.setComponentAlignment(headerLayout, Alignment.MIDDLE_CENTER);
 		headerLayout.setWidth("990px");
 		headerLayout.setHeight("81px");
 
 		// logo (image)
 		Embedded logoImage = new Embedded("", new ThemeResource("img/logo.png"));
 		headerLayout.addComponent(logoImage);
 		logoImage.setAlternateText("Gattserver");
 		logoImage.setStyleName("logo_image");
 
 		buildHeader(headerLayout);
 	}
 
 	protected void buildHeader(HorizontalLayout headerLayout) {
 	}
 
 	protected abstract void buildBody(VerticalLayout layout);
 
 	private void buildFooter(VerticalLayout layout) {
 
 		HorizontalLayout footerLayout = new HorizontalLayout();
 		layout.addComponent(footerLayout);
 		layout.setComponentAlignment(footerLayout, Alignment.BOTTOM_CENTER);
 
 		footerLayout.setWidth("100%");
 		footerLayout.setHeight("25px");
 		footerLayout.setStyleName("footer_layout");
 
 		Label footerNote = new Label("GRASS3 Copyright Hynek Uhlíř 2012");
 		footerLayout.addComponent(footerNote);

		HorizontalLayout footerShadow = new HorizontalLayout();
		layout.addComponent(footerShadow);
		layout.setComponentAlignment(footerShadow, Alignment.BOTTOM_CENTER);
		footerShadow.setStyleName("footer_shadow");
		footerShadow.setWidth("100%");
		footerShadow.setHeight("11px");
 	}
 
 }
