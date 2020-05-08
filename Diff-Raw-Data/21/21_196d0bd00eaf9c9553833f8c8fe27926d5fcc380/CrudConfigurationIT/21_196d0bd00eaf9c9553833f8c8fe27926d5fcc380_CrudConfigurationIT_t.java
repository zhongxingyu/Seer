 package org.motechproject.whp.reports.webservice.controller.it;
 
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.crud.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:META-INF/spring/applicationContext.xml"})
 public class CrudConfigurationIT {

    @Autowired
    CrudService crudService;

    @Test
    public void shouldLoadCrudEntities() {
        assertNotNull(crudService.getCrudEntity("District"));
        assertNotNull(crudService.getCrudEntity("DoNotCallEntry"));
    }
 }
