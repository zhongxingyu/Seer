 /*
  * Copyright (C) 2011-2013 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 3 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 @Application(defaultController = Home.class,name = "AcceptanceApplication")
 @Servlet("/")
 
 @Assets({
     @Asset(id = "jquery.js", value = "//code.jquery.com/jquery-1.10.2.min.js", location = AssetLocation.URL),
     @Asset(id = "underscore.js", value = "//cdnjs.cloudflare.com/ajax/libs/underscore.js/1.5.2/underscore-min.js", location = AssetLocation.URL),
     @Asset(id = "backbone.js", value = "//cdnjs.cloudflare.com/ajax/libs/backbone.js/1.0.0/backbone-min.js", location = AssetLocation.URL, depends = "underscore.js"),
     @Asset(id = "bootstrap.js", value = "//netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js", location = AssetLocation.URL, depends = "jquery.js"),
     @Asset(id = "acceptance.js", value = "acceptance.js", depends = {"jquery.js", "underscore.js", "bootstrap.js", "backbone.js"}),
     @Asset(id = "acceptance.css", value = "acceptance.css"),
     @Asset(id = "signin.css", value = "signin.css")
 }) package org.exoplatform.acceptance;
 
 import juzu.Application;
 import juzu.asset.AssetLocation;
 import juzu.plugin.asset.Asset;
 import juzu.plugin.asset.Assets;
 import juzu.plugin.servlet.Servlet;
 import org.exoplatform.acceptance.controllers.Home;
