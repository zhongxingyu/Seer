 package com.pillartechnology.speakerrate.controller;
 
import static org.junit.Assert.*;
 import static org.hamcrest.Matchers.*;
import org.junit.*;
 
 public class PresentationControllerTest {
 
	private PresentationController sut = new PresentationController();
 
 	@Test
	public void shouldRedirectToList() {
		
		sut.setPresentationRepository(null);
		
		String result = sut.list(null);
 		
		assertThat(result, is("presentation/list.jsp"));
 	}
 }
