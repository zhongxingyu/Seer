 package info.harmia.polyglot.springapp.mvc.web.controller;
 
 import info.harmia.polyglot.springapp.mvc.core.service.MunicipalityService;
 import org.json.JSONException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * Created with IntelliJ IDEA.
  * User: harmia
  * Date: 9.5.2013
  * Time: 10:34
  * Copyright (C) 2013 Juhana "harmia" Harmanen
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 @Controller
 public class MunicipalityController {
     @Autowired
     private MunicipalityService municipalityService;
 
    @RequestMapping(value = "api/municipalities/{term}", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
     @ResponseBody
     public String checkMunicipalitiesJson(@PathVariable("term") String term) throws JSONException {
         return municipalityService.checkMunicipalitiesJson(term).toString();
     }
 
    @RequestMapping(value = "api/municipalities", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
     @ResponseBody
    public String listMunicipalitiesJson() throws JSONException {
         return municipalityService.listMunicipalitiesJson().toString();
     }
 }
