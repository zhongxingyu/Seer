 package net.thucidides.fragments.elements;
 
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.pagefactory.ElementLocator;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 public class Select extends Fragment {
 
 	private org.openqa.selenium.support.ui.Select delegate() {
 		return new org.openqa.selenium.support.ui.Select(getWrappedElement());
 	}
 
 	public void deselectAll() {
 		publishEvent("deselecting all options");
 		delegate().deselectAll();
 	}
 
 	public void deselectByIndex(int index) {
 		publishEvent(String.format("deseclting [%s] option", index));
 		delegate().selectByIndex(index);
 	}
 
 	public void deselectByValue(String value) {
 		publishEvent(String.format("deselecting option with value [%s]", value));
 		delegate().deselectByValue(value);
 	}
 
	public void deselectBContextyVisibleText(String text) {
 		publishEvent(String.format("deselecting option with text [%s]", text));
 		delegate().deselectByVisibleText(text);
 	}
 
 	public <E extends Fragment<?>> Collection<E> getAllSelectedOptions(Class<E> clazz) {
 		return fragmentFactory.createList(clazz, new ElementLocator() {
 			public List<WebElement> findElements() { return delegate().getAllSelectedOptions(); }
 			public WebElement findElement() { return null; }
 		}, getName() + "-selected-option-");
 	}
 
 	public <E extends Fragment<?>> E getFirstSelectedOption(Class<E> clazz) {
 		return fragmentFactory.createFragment(clazz, new ElementLocator() {
 			public List<WebElement> findElements() { return null; }
 			public WebElement findElement() { return delegate().getFirstSelectedOption(); }
 		}, getName() + "-selected-option-1");
 	}
 	
 	public List<String> getSelectedOptionsText(){
 		List<WebElement> elements = delegate().getAllSelectedOptions();
 		
 		List<String> options = new ArrayList<>();
 		
 		for(WebElement element: elements){
 			options.add(element.getText());
 		}
 		
 		return options;
 	}
 	
 	public List<String> getOptionsText(){
 		List<WebElement> elements = delegate().getOptions();
 		
 		List<String> options = new ArrayList<>();
 		
 		for(WebElement element: elements){
 			options.add(element.getText());
 		}
 		
 		return options;
 	}
 	
 	public <E extends Fragment<?>> Collection<E> getOptions(Class<E> clazz) {
 		return fragmentFactory.createList(clazz, new ElementLocator() {
 			public List<WebElement> findElements() { return delegate().getOptions(); }
 			public WebElement findElement() { return null; }
 		}, getName() + "-option");
 	}
 
 	public boolean isMultiple() {
 		return delegate().isMultiple();
 	}
 
 	public void selectByIndex(int index) {
 		publishEvent(String.format("selecting option with index [%s]", index));
 		delegate().selectByIndex(index);
 	}
 
 	public void selectByValue(String value) {
 		publishEvent(String.format("selecting option with value [%s]", value));
 		delegate().selectByValue(value);
 	}
 
 	public void selectByVisibleText(String text) {
 		publishEvent(String.format("selecting option with text [%s]", text));
 		delegate().selectByVisibleText(text);
 	}
 }
