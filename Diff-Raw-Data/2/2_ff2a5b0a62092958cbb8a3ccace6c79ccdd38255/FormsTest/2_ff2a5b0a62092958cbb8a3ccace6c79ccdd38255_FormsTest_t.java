 package org.whizu.jquery.mobile;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 import org.whizu.ui.ClickListener;
 import org.whizu.value.StringValue;
 
 public class FormsTest extends AbstractJqmTest {
 
 	@Test
 	public void testButtonBuilder() {
 		Page next = Jqm.addPage("next");
 
 		StringValue name = new StringValue("Name");
 
 		// @formatter:off
 		Header.builder()
 		    .title("Forms")
 		    .build()
 		    .on(page);
 		// @formatter:on
 
 		Form form = new Form();
 		form.onSubmit(submit(name));
 		form.addText(name);
 		page.append(form);
 
 		// "$p = $(\"<div data-role='page' id='next'><div data-role='content'>page next</div></div>\"); $p.appendTo($.mobile.pageContainer); ;$('#index').prepend(\"<div data-role='header' id='c0'><h1>Forms</h1></div>\");$('#index').append(\"<form id='c1' method='post'><label for='c2'>Name</label><input id='c2' name='c2' value='' type='text'/></form>\");$(\"#c1\").submit(function(event) { event.preventDefault();alert('submit form');return false; });",
 
		String expected = "$p = $(\"<div data-role='page' id='next'><div data-role='content'>page next</div></div>\"); $p.appendTo($.mobile.pageContainer); ;$('#index').prepend(\"<div data-role='header' id='c0'><h1>Forms</h1></div>\");$('#index').append(\"<form id='c1' method='post'><label for='c3'>Name</label><input id='c3' name='c3' value='' type='text'/></form>\");$(\"#c1\").submit(function(event) { event.preventDefault();$.get('http://localhost:8090/whizu?id=c2', $(this).closest(\"form\").serialize(), function(data) {  }, 'script');return false; });";
 		assertEquals(expected, theRequest.finish());
 
 	}
 
 	private ClickListener submit(final StringValue name) {
 		return new ClickListener() {
 
 			@Override
 			public void click() {
 				Jqm.page().append("Name entered " + name.get());
 			}
 		};
 	}
 }
