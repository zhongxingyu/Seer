 package com.thoughtworks.lirenlab.integration;
 
 import com.thoughtworks.lirenlab.application.DonationService;
 import com.thoughtworks.lirenlab.domain.model.donation.Donation;
 import com.thoughtworks.lirenlab.domain.model.donation.DonationId;
 import com.thoughtworks.lirenlab.domain.model.donation.DonationRepository;
 import com.thoughtworks.lirenlab.domain.model.donation.DonationStatus;
 import org.hibernate.SessionFactory;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.List;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static com.thoughtworks.lirenlab.domain.model.device.DeviceId.deviceId;
 import static com.thoughtworks.lirenlab.domain.model.donation.Book.*;
 import static com.thoughtworks.lirenlab.domain.model.donation.PostSpecification.emptySpecification;
 import static com.thoughtworks.lirenlab.domain.model.donation.PostSpecification.postSpecification;
 import static org.hamcrest.CoreMatchers.hasItem;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration({"classpath:context.xml"})
 @TransactionConfiguration(defaultRollback = true)
 @Transactional
 public class DonationIntegrationTest {
 
     @Autowired
     private DonationService donationService;
 
     @Autowired
     private SessionFactory sessionFactory;
 
     @Autowired
     private DonationRepository donationRepository;
 
     @Test
     public void should_create_donation() throws Exception {
         DonationId donationId = donationService.newDonation(deviceId("12345"), newArrayList(newBook("isbn12345", "title")));
         Donation actual = donationRepository.find(donationId);
         assertThat(actual.deviceId(), is(deviceId("12345")));
         assertThat(actual.books(), hasItem(newBook("isbn12345", "title")));
     }
 
     @Test
     public void should_find_donations_of_specified_device() throws Exception {
         donationService.newDonation(deviceId("device"), newArrayList(newBook("isbn1", "title")));
         donationService.newDonation(deviceId("anotherDevice"), newArrayList(newBook("isbn12345", "title")));
         donationService.newDonation(deviceId("device"), newArrayList(newBook("isbn2", "title")));
         List<Donation> donations = donationRepository.find(deviceId("device"));
         List<Donation> anotherDonations = donationRepository.find(deviceId("anotherDevice"));
 
         assertThat(donations.size(), is(2));
         assertThat(anotherDonations.size(), is(1));
     }
 
     @Test
     public void should_reject_and_approve_book_of_donation_by_isbn() throws Exception {
         DonationId donationId = donationService.newDonation(deviceId("device"),
                 newArrayList(newBook("isbn1", "title1"), newBook("isbn2", "title2")));
 
         Donation donation = donationRepository.find(donationId);
         assertThat(donation.books().size(), is(2));
         assertThat(donation.books(), hasItem(approvedBook("isbn1", "title1")));
         assertThat(donation.books(), hasItem(approvedBook("isbn2", "title2")));
 
         donationService.rejectBook(donationId, "isbn1");
         Donation donationWithRejectedBook = donationRepository.find(donationId);
         assertThat(donationWithRejectedBook.books().size(), is(2));
         assertThat(donationWithRejectedBook.books(), hasItem(rejectedBook("isbn1", "title1")));
         assertThat(donationWithRejectedBook.books(), hasItem(approvedBook("isbn2", "title2")));
 
         donationService.approveBook(donationId, "isbn1");
         Donation donationWithApprovedBook = donationRepository.find(donationId);
         assertThat(donationWithApprovedBook.books().size(), is(2));
         assertThat(donationWithApprovedBook.books(), hasItem(approvedBook("isbn1", "title1")));
         assertThat(donationWithApprovedBook.books(), hasItem(approvedBook("isbn2", "title2")));
     }
 
     @Test
     public void should_update_post_specification() throws Exception {
         DonationId donationId = donationService.newDonation(deviceId("device1"),
                 newArrayList(newBook("isbn1", "title1")));
 
         Donation donation = donationRepository.find(donationId);
         assertThat(donation.postSpecification(), is(emptySpecification()));
 
         donationService.updatePostSpecification(donationId, postSpecification("Phone:124\nAddress:there\n"));
 
         Donation updatedDonation = donationRepository.find(donationId);
         assertThat(updatedDonation.postSpecification(), is(postSpecification("Phone:124\nAddress:there\n")));
     }
 
     @Test
     public void should_confirm_donation() throws Exception {
         DonationId donationId = donationService.newDonation(deviceId("device1"),
                 newArrayList(newBook("isbn1", "title1")));
 
         Donation donation = donationRepository.find(donationId);
         assertThat(donation.status(), is(DonationStatus.NEW));
 
        donationService.updatePostSpecification(donationId, postSpecification("some address"));
         donationService.confirm(donationId);
 
         Donation updatedDonation = donationRepository.find(donationId);
         assertThat(updatedDonation.status(), is(DonationStatus.APPROVED));
     }
 
 }
