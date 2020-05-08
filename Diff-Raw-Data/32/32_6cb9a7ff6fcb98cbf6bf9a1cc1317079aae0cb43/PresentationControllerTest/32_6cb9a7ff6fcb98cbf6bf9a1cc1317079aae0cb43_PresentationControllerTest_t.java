 package com.pillartechnology.speakerrate.controller;
 
 import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;

import com.pillartechnology.speakerrate.persistence.PresentationRepository;
 
@RunWith(MockitoJUnitRunner.class)
 public class PresentationControllerTest {
 
	@InjectMocks private PresentationController sut = new PresentationController();

	@Mock PresentationRepository presentationRepository;
	@Mock private Model model;
 
 	@Test
	public void shouldRedirectToIndex() {
		String result = sut.index(model);
 		
		assertThat(result, is("presentations/index"));
 	}
 }
