 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package};
 
 import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
 
public class ${name}Module extends AbstractPresenterModule {
     @Override
     protected void configure() {
        bindPresenter(${name}Presenter.class, ${name}Presenter.MyView.class, ${name}View.class, 
        		${name}Presenter.MyProxy.class);
     }
 }
