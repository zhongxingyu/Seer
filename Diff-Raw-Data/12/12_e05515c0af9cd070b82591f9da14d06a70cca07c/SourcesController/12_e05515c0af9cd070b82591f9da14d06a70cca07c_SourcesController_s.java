 /*
  * Copyright 2011 Damien Bourdette
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.dbourdette.otto.web.controller.sources;
 
 import java.io.UnsupportedEncodingException;
 
 import javax.inject.Inject;
 import javax.mail.MessagingException;
 import javax.validation.Valid;
 
 import org.apache.commons.lang.StringUtils;
 import org.bson.types.ObjectId;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.github.dbourdette.otto.data.DataTablePeriod;
 import com.github.dbourdette.otto.source.Source;
 import com.github.dbourdette.otto.source.TimeFrame;
 import com.github.dbourdette.otto.source.config.AggregationConfig;
 import com.github.dbourdette.otto.source.config.DefaultGraphParameters;
 import com.github.dbourdette.otto.source.config.TransformConfig;
 import com.github.dbourdette.otto.source.reports.ReportConfig;
 import com.github.dbourdette.otto.source.reports.ReportConfigs;
 import com.github.dbourdette.otto.source.schedule.MailSchedule;
 import com.github.dbourdette.otto.source.schedule.SourceScheduleExecutor;
 import com.github.dbourdette.otto.source.schedule.SourceSchedules;
 import com.github.dbourdette.otto.web.editor.ObjectIdEditor;
 import com.github.dbourdette.otto.web.form.CappingForm;
 import com.github.dbourdette.otto.web.form.IndexForm;
 import com.github.dbourdette.otto.web.form.SourceForm;
 import com.github.dbourdette.otto.web.form.TransformForm;
 import com.github.dbourdette.otto.web.util.FlashScope;
 import com.github.dbourdette.otto.web.util.IntervalUtils;
 
 /**
  * @author damien bourdette
  * @version \$Revision$
  */
 @Controller
 public class SourcesController {
 
     @Inject
     private FlashScope flashScope;
 
     @Inject
     private SourceScheduleExecutor scheduleExecutor;
 
     @Inject
     private SourceSchedules sourceSchedules;
 
 
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         binder.registerCustomEditor(ObjectId.class, new ObjectIdEditor());
     }
 
     @RequestMapping({"/sources/{name}/configuration"})
     public String source(@PathVariable String name, Model model) {
         Source source = Source.findByName(name);
 
         model.addAttribute("source", source);
         model.addAttribute("reports", ReportConfigs.forSource(source).getReportConfigs());
         model.addAttribute("schedules", sourceSchedules.findForSource(source));
         model.addAttribute("indexes", source.getIndexes());
 
         return "sources/source";
     }
 
     @RequestMapping({"/sources/{name}/statistics"})
     public String statistics(@PathVariable String name, Model model) {
         Source source = Source.findByName(name);
 
         model.addAttribute("source", source);
         model.addAttribute("lastWeekFrequency", source.findEventsFrequency(IntervalUtils.lastWeek()));
         model.addAttribute("yesterdayFrequency", source.findEventsFrequency(IntervalUtils.yesterday()));
         model.addAttribute("todayFrequency", source.findEventsFrequency(IntervalUtils.today()));
 
         return "sources/statistics";
     }
 
     @RequestMapping("/sources/form")
     public String form(Model model) {
         model.addAttribute("form", new SourceForm());
 
         return "sources/source_form";
     }
 
     @RequestMapping(value = "/sources", method = RequestMethod.POST)
     public String createSource(@Valid @ModelAttribute("form") SourceForm form, BindingResult result) {
         if (StringUtils.isNotEmpty(form.getSize())) {
             try {
                 form.getSizeInBytes();
             } catch (Exception e) {
                 result.rejectValue("size", "size.invalidPattern");
             }
         }
 
         if (Source.exists(form.getName())) {
             result.rejectValue("name", "source.alreadyExists");
         }
 
         if (result.hasErrors()) {
             return "sources/source_form";
         }
 
         Source.create(form);
 
         flashScope.message("source " + form.getName() + " has just been created");
 
         return "redirect:/sources/" + form.getName() + "/configuration";
     }
 
     @RequestMapping("/sources/{name}/edit")
     public String form(@PathVariable String name, Model model) {
         model.addAttribute("form", Source.findByName(name));
 
         return "sources/source_edit_form";
     }
 
     @RequestMapping(value = "/sources/edit", method = RequestMethod.POST)
     public String form(@Valid @ModelAttribute("form") SourceForm form, BindingResult result, Model model) {
         if (result.hasErrors()) {
             return "sources/source_edit_form";
         }
 
         Source source = Source.findByName(form.getName());
         source.setDisplayGroup(form.getDisplayGroup());
         source.setDisplayName(form.getDisplayName());
 
         return "redirect:/sources/" + form.getName() + "/configuration";
     }
 
     @RequestMapping("/sources/{name}/delete")
     public String dropSourceForm(@PathVariable String name, Model model) {
         return "sources/source_delete_form";
     }
 
     @RequestMapping(value = "/sources/{name}", method = RequestMethod.DELETE)
     public String dropSource(@PathVariable String name) {
         Source.findByName(name).delete();
 
         flashScope.message("source " + name + " has just been deleted");
 
         return "redirect:/sources";
     }
 
     @RequestMapping("/sources/{name}/aggregation/form")
     public String aggregation(@PathVariable String name, Model model) {
         AggregationConfig config = Source.findByName(name).getAggregationConfig();
 
         if (config == null) {
             config = new AggregationConfig();
         }
 
         model.addAttribute("form", config);
         model.addAttribute("timeFrames", TimeFrame.values());
 
         return "sources/aggregation_form";
     }
 
     @RequestMapping(value = "/sources/{name}/aggregation", method = RequestMethod.POST)
     public String saveAggregation(@PathVariable String name, @Valid @ModelAttribute("form") AggregationConfig form, BindingResult result, Model model) {
         if (result.hasErrors()) {
             model.addAttribute("timeFrames", TimeFrame.values());
 
             return "sources/aggregation_form";
         }
 
         Source source = Source.findByName(name);
         source.setAggregationConfig(form);
         source.save();
 
         flashScope.message("Aggregation has been modified for source " + name);
 
         return "redirect:/sources/{name}/configuration";
     }
 
     @RequestMapping("/sources/{name}/aggregation/delete")
     public String deleteAggregation(@PathVariable String name) {
         Source source = Source.findByName(name);
 
         source.setAggregationConfig(new AggregationConfig());
         source.save();
 
         return "redirect:/sources/{name}/configuration";
     }
 
     @RequestMapping("/sources/{name}/default-graph-params/form")
     public String defaultGraphParameters(@PathVariable String name, Model model) {
         model.addAttribute("form", Source.findByName(name).getDefaultGraphParameters());
         model.addAttribute("periods", DataTablePeriod.values());
 
         return "sources/default_graph_params_form";
     }
 
     @RequestMapping(value = "/sources/{name}/default-graph-params", method = RequestMethod.POST)
     public String saveDefaultGraphParameters(@PathVariable String name, @Valid @ModelAttribute("form") DefaultGraphParameters form, BindingResult result, Model model) {
         if (result.hasErrors()) {
             model.addAttribute("periods", DataTablePeriod.values());
 
             return "sources/default_graph_params_form";
         }
 
         flashScope.message("Default graph parameters have been set for source " + name);
 
         Source source = Source.findByName(name);
         source.setDefaultGraphParameters(form);
         source.save();
 
         return "redirect:/sources/{name}/configuration";
     }
 
     @RequestMapping("/sources/{name}/capping/form")
     public String capping(@PathVariable String name, Model model) {
         model.addAttribute("form", new CappingForm());
 
         return "sources/capping_form";
     }
 
     @RequestMapping(value = "/sources/{name}/capping", method = RequestMethod.POST)
     public String saveCapping(@PathVariable String name, @Valid @ModelAttribute("form") CappingForm form, BindingResult result, Model model) {
         if (StringUtils.isNotEmpty(form.getSize())) {
             try {
                 form.getSizeInBytes();
             } catch (Exception e) {
                 result.rejectValue("size", "size.invalidPattern");
             }
         }
 
         if (result.hasErrors()) {
             return "sources/capping_form";
         }
 
         flashScope.message("Source " + name + " has been capped");
 
         Source.findByName(name).cap(form);
 
         return "redirect:/sources/{name}/configuration";
     }
 
     @RequestMapping("/sources/{name}/report")
     public String report(@PathVariable String name, Model model) {
         model.addAttribute("form", new ReportConfig(name));
 
         return "sources/report_form";
     }
 
     @RequestMapping(value = "/sources/{name}/report", method = RequestMethod.POST)
     public String report(@PathVariable String name, @Valid @ModelAttribute("form") ReportConfig form, BindingResult result, Model model) {
         if (result.hasErrors()) {
             return "sources/report_form";
         }
 
         form.setSourceName(name);
         form.save();
 
         return "redirect:/sources/{name}/configuration";
     }
 
     @RequestMapping("/sources/{name}/report/{id}")
     public String report(@PathVariable String name, @PathVariable String id, Model model) {
         model.addAttribute("form", ReportConfigs.forSource(Source.findByName(name)).getReportConfig(id));
 
         return "sources/report_form";
     }
 
     @RequestMapping("/sources/{name}/report/{id}/delete")
     public String deleteReport(@PathVariable String name, @PathVariable String id) {
         ReportConfigs.forSource(Source.findByName(name)).getReportConfig(id).delete();
 
         return "redirect:/sources/{name}/configuration";
     }
 
     @RequestMapping("/sources/{name}/schedule")
     public String schedule(@PathVariable String name, Model model) {
         model.addAttribute("reports", ReportConfigs.forSource(Source.findByName(name)).getReportConfigs());
         model.addAttribute("form", new MailSchedule(name));
 
         return "sources/schedule/edit";
     }
 
     @RequestMapping(value = "/sources/{name}/schedule", method = RequestMethod.POST)
     public String schedule(@PathVariable String name, @Valid @ModelAttribute("form") MailSchedule form, BindingResult result, Model model) {
         if (!result.hasErrors()) {
            if (StringUtils.isEmpty(form.getSourceName())) {
                result.rejectValue("source", "value.notNull", "Can't be null");
            }

             if (StringUtils.isEmpty(form.getReport())) {
                 result.rejectValue("report", "value.notNull", "Can't be null");
             }
         }
 
         if (result.hasErrors()) {
             model.addAttribute("reports", ReportConfigs.forSource(Source.findByName(name)).getReportConfigs());
 
             return "sources/schedule/edit";
         }
 
         form.setSourceName(name);
 
         sourceSchedules.save(form);
 
         return "redirect:/sources/{name}/configuration";
     }
 
     @RequestMapping("/sources/{name}/schedule/{id}")
     public String schedule(@PathVariable String name, @PathVariable String id, Model model) {
         model.addAttribute("reports", ReportConfigs.forSource(Source.findByName(name)).getReportConfigs());
         model.addAttribute("form", sourceSchedules.findById(id));
 
         return "sources/schedule/edit";
     }
 
     @RequestMapping("/sources/{name}/schedule/{id}/delete")
     public String deleteSchedule(@PathVariable String name, @PathVariable String id) {
        sourceSchedules.save(sourceSchedules.findById(id));
 
         return "redirect:/sources/{name}/configuration";
     }
 
     @RequestMapping("/sources/{name}/schedule/{id}/send")
     public String sendSchedule(@PathVariable String name, @PathVariable String id) throws MessagingException, UnsupportedEncodingException {
         Source source = Source.findByName(name);
         MailSchedule schedule = sourceSchedules.findById(id);
 
         scheduleExecutor.execute(schedule);
 
         flashScope.message("Schedule " + schedule.getTitle() + " for source " + source.getName() + " has been executed (and mail sent)");
 
         return "redirect:/sources/{name}/configuration";
     }
 
     @RequestMapping("/sources/{name}/transform")
     public String transform(@PathVariable String name, Model model) {
         model.addAttribute("form", new TransformForm());
 
         return "sources/transform_form";
     }
 
     @RequestMapping(value = "/sources/{name}/transform", method = RequestMethod.POST)
     public String transform(@PathVariable String name, @Valid @ModelAttribute("form") TransformForm form, BindingResult result, Model model) {
         if (result.hasErrors()) {
             return "sources/transform_form";
         }
 
         Source source = Source.findByName(name);
 
         TransformConfig config = source.getTransformConfig();
 
         config.forParam(form.getParameter()).replace(form.getOperations());
 
         source.setTransformConfig(config);
         source.save();
 
         return "redirect:/sources/{name}/configuration";
     }
 
     @RequestMapping("/sources/{name}/transform/{parameter}")
     public String transform(@PathVariable String name, @PathVariable String parameter, Model model) {
         TransformConfig config = Source.findByName(name).getTransformConfig();
 
         TransformForm form = new TransformForm();
         form.setParameter(parameter);
         form.setOperations(config.getOperationsLiteral(parameter));
 
         model.addAttribute("form", form);
 
         return "sources/transform_form";
     }
 
     @RequestMapping("/sources/{name}/transform/{parameter}/delete")
     public String deleteTransform(@PathVariable String name, @PathVariable String parameter, Model model) {
         Source source = Source.findByName(name);
 
         source.getTransformConfig().forParam(parameter).remove();
 
         source.save();
 
         return "redirect:/sources/{name}/configuration";
     }
 
     @RequestMapping("/sources/{name}/indexes/form")
     public String indexes(@PathVariable String name, Model model) {
         model.addAttribute("form", new IndexForm());
 
         return "sources/mongodb_index/add";
     }
 
     @RequestMapping(value = "/sources/{name}/indexes", method = RequestMethod.POST)
     public String indexes(@PathVariable String name, @Valid @ModelAttribute("form") IndexForm form, BindingResult result, Model model) {
         if (result.hasErrors()) {
             return "sources/mongodb_index/add";
         }
 
         Source source = Source.findByName(name);
 
         source.createIndex(form);
 
         return "redirect:/sources/{name}/configuration";
     }
 
     @RequestMapping("/sources/{name}/indexes/{index}/drop")
     public String dropIndex(@PathVariable String name, @PathVariable String index) {
         Source source = Source.findByName(name);
 
         source.dropIndex(index);
 
         return "redirect:/sources/{name}/configuration";
     }
 }
