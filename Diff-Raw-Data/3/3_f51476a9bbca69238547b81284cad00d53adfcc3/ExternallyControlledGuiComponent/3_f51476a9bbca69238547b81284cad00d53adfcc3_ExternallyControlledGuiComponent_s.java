 /**
  * This file is part of Project Control Center (PCC).
  * 
  * PCC (Project Control Center) project is intellectual property of 
  * Dmitri Anatol'evich Pisarenko.
  * 
  * Copyright 2010, 2011 Dmitri Anatol'evich Pisarenko
  * All rights reserved
  *
  **/
 
 package ru.altruix.commons.api.gui;
 
 import com.vaadin.ui.AbstractComponent;
 
 /**
  * @author DP118M
  * 
  */
public interface ExternallyControlledGuiComponent<C extends GuiController<K>, K extends AbstractComponent> {
     void setGuiController(final C aController);
 }
